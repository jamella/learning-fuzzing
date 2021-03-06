package net.praseodym.activelearner;

import de.learnlib.api.SUL;
import de.learnlib.api.SULException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * External process System under Learning (SUL) wrapper
 */
public class ProcessSUL implements SUL<String, String>, DisposableBean {

    private final Logger logger = LoggerFactory.getLogger(ProcessSUL.class);

    public static final byte[] SEPARATOR = System.lineSeparator().getBytes();

    private Process p;
    private OutputStream stdin;
    private BufferedReader stdout;
    private int execs;

    @Value("${learner.target}")
    private String target;

    @Override
    public void destroy() throws Exception {
        logger.info("Total executions: {}", execs);
    }

    @Override
    public void pre() {
        logger.trace("Pre: initialise target");
        ProcessBuilder pb =  new ProcessBuilder(target);
        try {
            p = pb.start();
            execs++;
            stdin = p.getOutputStream();
            stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void post() {
        logger.trace("Post: destroy target");
        p.destroyForcibly();
    }

    @Nullable
    @Override
    public String step(@Nullable String in) throws SULException {
        logger.trace("stdin: {}", in);

        if (in == null)
            return null;

        if (!p.isAlive())
            return null;

        String out;
        try {
            stdin.write(in.getBytes());
            stdin.write(SEPARATOR);
            stdin.flush();

            // FIXME: ugly hack to fix multi-line output
            out = stdout.readLine();
            if (stdout.ready()) {
                out += "_" + stdout.readLine();
            }
        } catch (IOException e) {
            logger.trace("IOException ({}) - Process aliveness: {}", e.getMessage(), p.isAlive());
            if ("Broken pipe".equals(e.getMessage()) || "Stream closed".equals(e.getMessage())) {
                return null;
            } else
                throw new SULException(e);
        }

        logger.trace("stdout: {}", out);

        return out;
    }

}

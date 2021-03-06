package net.praseodym.activelearner;

import de.learnlib.experiments.Experiment;
import de.learnlib.statistics.SimpleProfiler;
import de.learnlib.statistics.StatisticData;
import de.learnlib.statistics.StatisticOracle;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.words.Alphabet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Mealy Machine Learner class
 */
@Component
public class MealyMachineLearner implements CommandLineRunner, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(MealyMachineLearner.class);

    @Resource
    private Alphabet<String> alphabet;

    @Autowired
    private Experiment.MealyExperiment<String, String> experiment;

    @Autowired
    private StatisticOracle[] statisticOracles;

    @Value("${learner.outdir:}")
    private String outdir;

    private Path outputDirectory;

    @Override
    public void afterPropertiesSet() {
        outputDirectory = Paths.get(outdir);
        assert Files.exists(outputDirectory) : "Output directory does not exist";
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting learning");

        experiment.setProfile(true);
        // We only save the last 100 hypotheses so as to not fill the disk
        experiment.setHypothesesHandler((round, model) -> saveModel(String.format("hypothesis%02d", round % 100),
                model));

        long start = System.nanoTime();
        experiment.run();
        long end = System.nanoTime();
        long d = (end - start) / 1000000;

        // Save learned model
        MealyMachine result = experiment.getFinalHypothesis();
        Path finalModel = saveModel("learnedModel", result);
        //SimplifyDot.simplifyDot(finalModel, outputDirectory.resolve("learnedModel-simple.dot"));

        // Build statistics report
        String line = "-------------------------------------------------------\n";
        StringBuilder sb = new StringBuilder(line);
        sb.append(String.format("Total time: %d ms = %.2f s = %.2f h = %.2f d\n\n", d, d / 1000.0, d / 3600000.0, d /
                86400000.0));
        sb.append(SimpleProfiler.getResults()).append("\n");
        sb.append(experiment.getRounds().getSummary()).append("\n\n");
        Arrays.stream(statisticOracles).map(StatisticOracle::getStatisticalData).map(StatisticData::getSummary)
                .sorted().forEach(s -> sb.append(s).append("\n"));
        sb.append("\nStates in final hypothesis: ").append(result.size()).append("\n");
        sb.append("Output directory: ").append(outputDirectory.toAbsolutePath()).append("\n");
        sb.append(line);

        // Print report
        String report = sb.toString();
        logLines(report);
        System.err.println(report);
    }

    private void logLines(String summary) {
        Arrays.stream(summary.split(System.lineSeparator())).forEach(log::info);
    }

    @SuppressWarnings("unchecked")
    private Path saveModel(String modelName, MealyMachine mealyMachine) {
        try {
            Path destination = outputDirectory.resolve(modelName + ".dot");
            log.info("Model {} has {} states", destination.getFileName(), mealyMachine.size());
            PrintStream psDotFile = new PrintStream(destination.toFile());
            GraphDOT.write(mealyMachine, alphabet, psDotFile);
            return destination;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

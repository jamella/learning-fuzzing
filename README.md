# Combining learning and fuzzing

This repository contains:
* **activelearner**: Prototype tool that uses LearnLib for active learning and that integrates with our _libafl_ library for fuzzing.
* **afl**: American Fuzzy Lop (AFL) fuzzer, with the _libafl_ library that is used in prototype tool.
* **benchmarks**: A set of learning benchmarks, built with cgenerate.
* **fsmutils**: FSM utilities by Rick Smetsers: mintrace is a tool that checks whether a given FSM is minimal and determines minimal separating sequence, cgenerate generates a C program that implements the state machine given by an input dot file.
* **passivelearner**: Tool to convert a set of input files to traces with input and output for use with passive learning.
* **rers2015**: Rigorous Examination of Reactive Systems (RERS) Challenge 2015 code with build files and various fixes (patches).
* **simpletarget**: Simple state machine target, used to test learning implementation.

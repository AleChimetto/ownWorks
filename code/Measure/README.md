
MATLAB SCRIPTS

	-evaluate_prof_formula compute the mean nDCG between topics with the formula seen at lesson.
	-evaluate_prof_formula compute the mean nDCG between topics with the formula equal to evaluate.py given by clef organizers. To know the difference ask to Alessio Zanardelli or inspect the evaluation script evaluate.py (download from https://webis.de/events/touche-22/shared-task-2.html).

RUN MATLAB SCRIPTS

	In order to run the matlab scripts, at the top there are three vars to be modified: 
		-path_run_file: path to the file run.txt, format [qid stance doc rank score tag], rank starting from 1.
		-path_qrels: path to relevance.qrels ore judjments.qrels format [qid 0 doc rel].
		-depth: number of docs to be consider in the computation of ndcg.

PAY ATTENTION:
 	-the scripts do not handle multiple runs.
	-the scripts works if the rank of the docs starts from number 1.
	-the scripts do not handle any type of error.
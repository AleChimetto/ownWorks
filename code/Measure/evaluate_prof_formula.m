%
%   THE MATLAB SCRIPT COMPUTE THE AVERAGE NDCG OF THE RUN.
%
clear;
clc;

% INPUT VARS
% path_run_file: path to run.txt format [qid stance doc rank score tag], rank starting from 1.
% path_qrels: path to relevance.qrels ore judjments.qrels format [qid 0 doc rel].
% depth: number of docs to be consider in the computation of ndcg.

path_run_file = "testRun.txt";
path_qrels = "test-relevance.qrels";
depth;

% LOAD DATA
RUN = readRun(path_run_file, depth);
QRELS = readQrels(path_qrels);

% ALL THE COMPUTATIONS ARE PERFORMED BY TABLE MODIFICATIONS

% iDCG COMPUTATION PER TOPIC PROF FORMULA
iDCG_prof = varfun(@(x) idcg_prof(x,depth), QRELS(:,["qid","rel"]),"GroupingVariables", 'qid','OutputFormat','table');
iDCG_prof.Properties.VariableNames("Fun_rel") = "idcg_prof";
iDCG_prof = iDCG_prof(:,{'qid','idcg_prof'});

% DCG COMPUTATION PER TOPIC PROF FORMULA

% Prepare data
DCG_prof = RUN(:,["qid","doc","rank"]);
DCG_prof = groupfilter(DCG_prof, 'qid', @(x) x<=depth,'rank');
DCG_prof = innerjoin(DCG_prof,QRELS,"Keys",["qid","doc"]);

% % % % DCG = RUN(:,["qid","doc","rank"]);
% % % % DCG = sortrows(DCG,["qid","rank"],["ascend","ascend"])
% % % % DCG = groupfilter(DCG,"qid",@(x) select_head(x,depth),"rank");
% % % % DCG = innerjoin(DCG,QRELS,"Keys",["qid","doc"])

% Perform computation
DCG_prof = rowfun(@(x,y) dcg_prof(x,y),DCG_prof,"GroupingVariables",'qid','InputVariables',{'rel','rank'},'OutputVariableNames','dcg_prof');
DCG_prof = DCG_prof(:,["qid","dcg_prof"]);

% nDCG COMPUTATION PER TOPIC PROF FORMULA

% Prepare data
nDCG_prof = join(DCG_prof,iDCG_prof);

% Perform calculation
nDCG_prof = rowfun(@(x,y) x./y, nDCG_prof,"GroupingVariables",'qid','InputVariables',{'dcg_prof','idcg_prof'},'OutputVariableNames','ndcg_prof');
nDCG_prof = nDCG_prof(:,["qid","ndcg_prof"]);

% GROUPING RESULTS TOGETHER
TMeasure_prof = join(DCG_prof,join(iDCG_prof,nDCG_prof));
TMeasure_prof = sortrows(TMeasure_prof,"ndcg_prof","ascend")

% MEAN OF nDCG BETWEEN TOPIC
meanDCG_prof = varfun(@mean,TMeasure_prof,"InputVariables","ndcg_prof",OutputFormat="uniform")


% FUNCTIONS

function result = idcg_prof(rel, depth)
    rel_depth = topkrows(rel, depth);
    rank = (1:1:length(rel_depth)).';
    result = dcg_prof(rel_depth,rank);
end

% The function compute the dcg given the relevance and the rank of the doc
% as column vectors
function DCG = dcg_prof(rel, rank)
    den = max(1,log2(double(rank))); %prof measure
    DCG = sum(double(rel)./den);
end

function RUN = readRun(path_run_file, depth)
    run_file = fopen(path_run_file,'rt');
    formatSpec = '%d %s %s %d %f %s';
    DATA = textscan(run_file,formatSpec);
    RUN = table(DATA{1,1}, DATA{1,3}, DATA{1,4}, DATA{1,5}, DATA{1,6});
    RUN.Properties.VariableNames = {'qid', 'doc', 'rank', 'score', 'tag'};
    
    fclose(run_file); 
end

function QRELS = readQrels(path_qrels)
    qrels_file = fopen(path_qrels,'r');
    formatSpec = '%d %d %s %d';
    DATA = textscan(qrels_file,formatSpec);
    QRELS = table(DATA{1,1}, DATA{1,3}, DATA{1,4});
    QRELS.Properties.VariableNames = {'qid', 'doc', 'rel'};
    fclose(qrels_file); 

end

function result = select_head(x,depth)
    if (length(x)<depth) 
        result = true(length(x),1);
    else 
        result = [true(depth,1);false(length(x)-depth,1)];
    end
end

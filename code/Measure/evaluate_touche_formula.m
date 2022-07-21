%
%   THE MATLAB SCRIPT COMPUTE THE AVERAGE NDCG OF THE RUN.
%
clear;
clc;

% INPUT VARS
% path_run_file : path to run.txt format [qid stance doc rank score tag], rank starting from 1; 
% path_qrels : path to relevance.qrels ore judjments.qrels format [qid 0 doc rel]
% depth : number of docs to be consider in the computation of ndcg

path_run_file = "testRun.txt";
path_qrels = "test-relevance.qrels";
depth = 5;

% LOAD DATA
RUN = readRun(path_run_file, depth);
QRELS = readQrels(path_qrels);

% ALL THE COMPUTATIONS ARE PERFORMED BY TABLE MODIFICATIONS

% iDCG COMPUTATION PER TOPIC TOUCHE FORMULA
iDCG_touche = varfun(@(x) idcg_touche(x,depth), QRELS(:,["qid","rel"]),"GroupingVariables", 'qid','OutputFormat','table');
iDCG_touche.Properties.VariableNames("Fun_rel") = "idcg_touche";
iDCG_touche = iDCG_touche(:,{'qid','idcg_touche'});

% DCG COMPUTATION PER TOPIC TOUCHE FORMULA

% Prepare data
DCG_touche = RUN(:,["qid","doc","rank"]);
DCG_touche = groupfilter(DCG_touche, 'qid', @(x) x<=depth,'rank');
DCG_touche = innerjoin(DCG_touche,QRELS,"Keys",["qid","doc"]);

% % % % DCG = RUN(:,["qid","doc","rank"]);
% % % % DCG = sortrows(DCG,["qid","rank"],["ascend","ascend"])
% % % % DCG = groupfilter(DCG,"qid",@(x) select_head(x,depth),"rank");
% % % % DCG = innerjoin(DCG,QRELS,"Keys",["qid","doc"])

% Perform computation
DCG_touche = rowfun(@(x,y) dcg_touche(x,y),DCG_touche,"GroupingVariables",'qid','InputVariables',{'rel','rank'},'OutputVariableNames','dcg_touche');
DCG_touche = DCG_touche(:,["qid","dcg_touche"]);

% nDCG COMPUTATION PER TOPIC TOUCHE FORMULA

% Prepare data
nDCG_touche = join(DCG_touche,iDCG_touche);

% Perform calculation
nDCG_touche = rowfun(@(x,y) x./y, nDCG_touche,"GroupingVariables",'qid','InputVariables',{'dcg_touche','idcg_touche'},'OutputVariableNames','ndcg_touche');
nDCG_touche = nDCG_touche(:,["qid","ndcg_touche"]);

% GROUPING RESULTS TOGETHER
TMeasure_touche = join(DCG_touche,join(iDCG_touche,nDCG_touche));
TMeasure_touche = sortrows(TMeasure_touche,"ndcg_touche","ascend")

% MEAN OF nDCG BETWEEN TOPIC
meanDCG_touche = varfun(@mean,TMeasure_touche,"InputVariables","ndcg_touche",OutputFormat="uniform")


% FUNCTIONS

function result = idcg_touche(rel, depth)
    rel_depth = topkrows(rel, depth);
    rank = (0:1:length(rel_depth)-1).';
    result = dcg_touche(rel_depth,rank);
end

function DCG = dcg_touche(rel, rank)
    [rank, index] = sort(rank);
    rel = rel(index);
    den = ((0:1:length(rel)-1)).';
    den = log2(2+den);
    DCG = sum(double (rel)./den);
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

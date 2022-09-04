# TODO: CHANGE THIS FILE NAME TO DMA_project2_team##.py
# EX. TEAM 1 --> DMA_project2_team01.py

# TODO: IMPORT LIBRARIES NEEDED FOR PROJECT 2
import mysql.connector
import os
import surprise
from surprise import Dataset
from surprise import Reader
from collections import defaultdict
import numpy as np
import pandas as pd
from sklearn import tree
import graphviz
from mlxtend.frequent_patterns import association_rules, apriori
from surprise import SVD, SVDpp, NMF, KNNBasic, KNNBaseline, KNNWithMeans, KNNWithZScore
from surprise.model_selection import cross_validate
from surprise.model_selection import KFold
np.random.seed(0)

# TODO: CHANGE GRAPHVIZ DIRECTORY
os.environ["PATH"] += os.pathsep + 'C:/Program Files (x86)/Graphviz2.38/bin/'

# TODO: CHANGE MYSQL INFORMATION, team number 
HOST = 'localhost'
USER = 'root'
PASSWORD = 'password'
SCHEMA = 'schema'
team = 6


# PART 1: Decision tree 
def part1():
    cnx = mysql.connector.connect(host=HOST, user=USER, password=PASSWORD)
    cursor = cnx.cursor()
    cursor.execute('SET GLOBAL innodb_buffer_pool_size=2*1024*1024*1024;')
    cursor.execute('USE %s;' % SCHEMA)
    
    # TODO: Requirement 1-1. MAKE pro_mentor column
    # Column 삽입
    try:
        cursor.execute('ALTER TABLE mentor ADD pro_mentor TINYINT(1) DEFAULT 0;')
    except mysql.connector.errors.ProgrammingError:
        # 이미 pro_mentor 열이 삽입된 경우 에러 방지
        pass

    # pro_mentor list 파일 내의 id list 받아오기
    f = open('pro_mentor_list.txt', 'r', encoding='UTF8')
    pro_list = f.readlines()
    f.close()

    # pro_mentor list를 담는 임시 테이블 삽입 및 데이터 저장
    cursor.execute('''
       CREATE TABLE temp(
           id VARCHAR(255) PRIMARY KEY);''')
    for pro_id in pro_list:
        cursor.execute('INSERT INTO temp(`id`) VALUES("%s") ' % (pro_id.strip()))
    cnx.commit()

    # 멘토의 id가 임시 테이블에 있는 경우 데이터 변경
    cursor.execute('''
       UPDATE mentor
       SET pro_mentor = 1
       WHERE mentor.id IN (
       SELECT tt.id FROM(
       SELECT t.id FROM temp AS t) AS tt);''')
    cnx.commit()

    # 임시 테이블 삭제
    cursor.execute("DROP TABLE temp;")
    cnx.commit()
    
    # -------
    
    
    # TODO: Requirement 1-2. WRITE MYSQL QUERY AND EXECUTE. SAVE to .csv file

    fopen = open('DMA_project2_team%02d_part1.csv' % team, 'w', encoding='utf-8')
    # SQL문 실행(SQL문의 자세한 설명은 보고서 참조)
    cursor.execute('''
       SELECT mentor.id, mentor.pro_mentor, 
       TRUNCATE((unix_timestamp('2020-01-01 00:00:00') - unix_timestamp(mentor.joined_date))/3600,0) AS age, 
       (1 - isnull(mentor.introduction))  AS have_introduction,
       (1 - isnull(mentor.field)) AS have_field,
       AA.noa AS num_of_answers,
       AA.aoas AS avg_of_answer_score,
       AA.aoab AS avg_of_answer_body,
       BB.nog AS num_of_groups,
       BB.aogm AS avg_of_group_members,
       CC.noe AS num_of_emails,
       DD.nt AS num_of_tags
       FROM mentor, 
       (SELECT mentor.id, IFNULL(A.ans_cnt, 0) AS noa, IFNULL(A.sco_avg, 0) AS aoas, IFNULL(A.bod_avg, 0) AS aoab 
       FROM mentor LEFT JOIN
       (SELECT mentor_id, COUNT(*) AS ans_cnt, AVG(score) AS sco_avg, AVG(body) AS bod_avg FROM answer GROUP BY mentor_id) AS A
       ON mentor.id = A.mentor_id) AS AA,
       (SELECT mentor.id, IFNULL(B.gr_cnt, 0) AS nog, IFNULL(B.avg_mte, 0) AS aogm 
       FROM mentor LEFT JOIN
       (SELECT mg.mentor, mg.id, count(mg.mentor) AS gr_cnt, AVG(mte.mte_cnt) AS avg_mte FROM mentoring_group as mg 
       INNER JOIN
       (SELECT group_id, count(*) AS mte_cnt FROM group_membership GROUP BY group_id) AS mte
       ON mg.id = mte.group_id GROUP BY mentor) AS B
       ON mentor.id = B.mentor) AS BB,
       (SELECT mentor.id, IFNULL(C.e_cnt, 0) AS noe 
       FROM mentor LEFT JOIN 
       (SELECT recipient_id, count(*) AS e_cnt FROM email GROUP BY recipient_id) AS C
       ON mentor.id = C.recipient_id) AS CC,
       (SELECT mentor.id, IFNULL(D.tag_cnt,0) AS nt 
       FROM mentor LEFT JOIN
       (SELECT mentor_id, count(*) AS tag_cnt FROM tag_mentor GROUP BY mentor_id) AS D
       ON mentor.id = D.mentor_id) AS DD
       WHERE mentor.id = AA.id AND mentor.id = BB.id AND mentor.id = CC.id AND mentor.id = DD.id;''')

    # 결과 반환하고 id 순으로 재정렬
    query_result = cursor.fetchall()
    query_results = sorted(query_result, key=lambda x: x[0])

    # csv 파일에 결과 작성
    fopen.write(
        'id,pro_mentor,age,have_introduction,have_field,num_of_answers,avg_of_answer_score,avg_of_answer_body,num_of_groups,avg_of_group_members,num_of_emails,num_of_tags\n')

    for data in query_results:
        line = ""
        for i in range(len(data)):
            line += str(data[i])
            if i != len(data) - 1:
                line += ","
            else:
                line += "\n"
        fopen.write(line)

    fopen.close()
 
    
    # -------
    
    
    # TODO: Requirement 1-3. MAKE AND SAVE DECISION TREE
    # gini file name: DMA_project2_team##_part1_gini.pdf
    # entropy file name: DMA_project2_team##_part1_entropy.pdf

    # 1-2에서 작성한 파일을 pandas의 DataFrame 자료형으로 다시 호출
    part1_df = pd.read_csv("DMA_project2_team06_part1.csv")

    # 의사결정나무를 만들기 위한 사전 준비
    feature_names = ['age', 'have_introduction', 'have_field', 'num_of_answers', 'avg_of_answer_score',
                     'avg_of_answer_body', 'num_of_groups', 'avg_of_group_members', 'num_of_emails', 'num_of_tags']

    X_train = part1_df[feature_names]
    y_train = part1_df['pro_mentor']

    # gini 계수를 기준으로 사용한 의사결정나무 생성
    gini_model = tree.DecisionTreeClassifier(criterion='gini', max_depth=5, min_samples_leaf=10)
    gini_model.fit(X_train, y_train)

    # 생성된 의사결정나무 시각화
    gini_graph = tree.export_graphviz(gini_model, feature_names=feature_names, class_names=['normal', 'PRO'],
                                    out_file=None)

    # pdf 파일 변환 작업
    gini_graph = graphviz.Source(gini_graph)
    gini_graph.render('DMA_project2_team06_part1_gini')

    # entropy를 기준으로 한 의사결정나무도 마찬가지로 작업
    entropy_model = tree.DecisionTreeClassifier(criterion='entropy', max_depth=5, min_samples_leaf=10)
    entropy_model.fit(X_train, y_train)

    entropy_graph = tree.export_graphviz(entropy_model, feature_names=feature_names, class_names=['normal', 'PRO'],
                                    out_file=None)

    entropy_graph = graphviz.Source(entropy_graph)
    entropy_graph.render('DMA_project2_team06_part1_entropy')

    # -------
    
    # TODO: Requirement 1-4. Don't need to append code for 1-4
    # 전체 feature 10개 중 6개 선정
    new_feature_names = ['age', 'num_of_answers', 'avg_of_answer_score', 'avg_of_answer_body',
                         'num_of_emails', 'num_of_tags']

    X_train = part1_df[new_feature_names]

    # 기준은 gini로, max_depth = 7로 의사결정나무 생성
    new_model = tree.DecisionTreeClassifier(criterion='gini', max_depth=7, min_samples_leaf=10)
    new_model.fit(X_train, y_train)

    new_graph = tree.export_graphviz(new_model, feature_names=new_feature_names, class_names=['normal', 'PRO'],
                                    out_file=None)

    new_graph = graphviz.Source(new_graph)
    new_graph.render('DMA_project2_team06_part1_1-4')

    # -------
    
    cursor.close()
    

# PART 2: Association analysis
def part2():
    cnx = mysql.connector.connect(host=HOST, user=USER, password=PASSWORD)
    cursor = cnx.cursor()
    cursor.execute('SET GLOBAL innodb_buffer_pool_size=2*1024*1024*1024;')
    cursor.execute('USE %s;' % SCHEMA)
    
    # TODO: Requirement 2-1. CREATE VIEW AND SAVE to .csv file
    cursor.execute(
        """
        CREATE VIEW tag_score AS
        SELECT tag.id AS tag_id, tag.name AS tag_name,
        IF(MENTOR.C is NULL, 0, MENTOR.C) AS num_mentor,
        IF(MENTEE.C is NULL, 0, MENTEE.C) AS num_mentee,
        IF(QUES.C is NULL, 0, QUES.C) AS num_question,
        IF(MENTOR.C is NULL, 0, MENTOR.C) + IF(MENTEE.C is NULL, 0, MENTEE.C) + IF(QUES.C is NULL, 0, QUES.C) AS score
        FROM tag
        LEFT JOIN (
        SELECT tag_id ,count(*) AS C
        FROM tag_mentor
        GROUP BY tag_id) AS MENTOR
        ON tag.id = MENTOR.tag_id
        LEFT JOIN (
        SELECT tag_id, count(*) AS C
        FROM tag_mentee
        GROUP BY tag_id) AS MENTEE
        ON tag.id = MENTEE.tag_id
        LEFT JOIN (
        SELECT tag_id, count(*) AS C
        FROM tag_question
        GROUP BY tag_id) AS QUES
        ON tag.id = QUES.tag_id
        ORDER BY score DESC
        LIMIT 50;
        """)

    cursor.execute("SELECT * from tag_score")
    tag_score = pd.DataFrame(cursor.fetchall())
    tag_score.columns = cursor.column_names
    tag_score = tag_score.set_index('tag_id')
    tag_score.to_csv('DMA_project2_team%02d_part2_tag.csv' % team, mode='w', encoding='utf-8')

    # ------
    
    # TODO: Requirement 2-2. CREATE 2 VIEWS AND SAVE partial one to .csv file 

    cursor.execute(
        """
        CREATE VIEW user_item_rating AS
            SELECT U1.user AS user, U1.item AS item, SUM(rating) AS rating
            FROM 
            (SELECT TMENTEE.mentee_id AS user, TS.tag_name AS item, 5*count(*) AS rating
            FROM tag_score AS TS
            JOIN tag_mentee AS TMENTEE
            ON TS.tag_id = TMENTEE.tag_id
            GROUP BY TMENTEE.mentee_id, TS.tag_name
            UNION ALL
            SELECT Q.mentee_id AS user, TS.tag_name AS item, LEAST(5, count(*)) AS rating
            FROM tag_score AS TS, tag_question AS TQ, question AS Q
            WHERE TS.tag_id = TQ.tag_id and TQ.question_id = Q.id
            GROUP BY Q.mentee_id, TS.tag_name
            ORDER BY user) AS U1
            GROUP BY U1.user, U1.item
    
            UNION ALL
    
            SELECT U2.user AS user, U2.item AS item, SUM(rating) AS rating
            FROM
            (SELECT TMENTOR.mentor_id AS user, TS.tag_name AS item, 5*count(*) AS rating
            FROM tag_score AS TS
            JOIN tag_mentor AS TMENTOR
            ON TS.tag_id = TMENTOR.tag_id
            GROUP BY TMENTOR.mentor_id, TS.tag_name
            UNION ALL
            SELECT A.mentor_id AS user, TS.tag_name AS item, LEAST(5, count(*)) AS rating
            FROM tag_score AS TS, tag_question AS TQ, answer AS A
            WHERE TS.tag_id = TQ.tag_id and TQ.question_id = A.question_id
            GROUP BY A.mentor_id, TS.tag_name
            ORDER BY USER) AS U2
            GROUP BY U2.user, U2.item;
        """)

    cursor.execute(
        """
        CREATE VIEW partial_user_item_rating AS
            SELECT UIR.user as user, UIR.item as item, UIR.rating as rating
            FROM user_item_rating AS UIR, 
            (SELECT UIR.user as user
            FROM user_item_rating AS UIR
            GROUP BY UIR.user
            HAVING count(UIR.rating) >= 4) AS PUIR
            WHERE UIR.user = PUIR.user;
    
        """)

    cursor.execute("SELECT * FROM partial_user_item_rating")

    partial_user_item_rating = pd.DataFrame(cursor.fetchall())
    partial_user_item_rating.columns = cursor.column_names
    partial_user_item_rating = partial_user_item_rating.set_index("user")
    partial_user_item_rating.to_csv('DMA_project2_team%02d_part2_UIR.csv' % team, mode='w', encoding='utf-8')

    # ------
    
    # TODO: Requirement 2-3. MAKE HORIZONTAL VIEW
    # file name: DMA_project2_team##_part2_horizontal.pkl

    cursor.execute("SELECT * FROM partial_user_item_rating")

    A = pd.DataFrame(cursor.fetchall())
    A.columns = cursor.column_names
    hor_view = pd.get_dummies(A['item'])
    hor_view1 = pd.concat([A['user'], hor_view], axis=1)
    user_id = list(set(hor_view1['user'].values))
    result = pd.DataFrame(columns=hor_view1.columns)
    for n, i in enumerate(user_id):
        result.loc[n] = hor_view1.loc[hor_view1['user']==i][hor_view.columns].sum(axis=0)
        result["user"][n] = i

    result = result.sort_values(by="user")
    hor_table = result.set_index('user')
    hor_table.to_pickle('DMA_project2_team%02d_part2_horizontal.pkl' % team)

    # ------
    
    # TODO: Requirement 2-4. ASSOCIATION ANALYSIS
    # filename: DMA_project2_team##_part2_association.pkl (pandas dataframe )

    frequent_itemsets = apriori(hor_table, min_support=0.01, use_colnames=True)
    rules = association_rules(frequent_itemsets, metric='lift', min_threshold=1)
    rules.to_pickle('DMA_project2_team%02d_part2_association.pkl' % team)
    # ------

    cursor.close()

    

# TODO: Requirement 3-1. WRITE get_top_n 
def get_top_n(algo, testset, id_list, n=10, user_based=True):
    
    results = defaultdict(list)
    if user_based:
        # TODO: testset의 데이터 중에 user id가 id_list 안에 있는 데이터만 따로 testset_id로 저장 
        # Hint: testset은 (user_id, item_id, default_rating)의 tuple을 요소로 갖는 list
        testset_id = []
        for i in testset:
            if i[0] in id_list:
                testset_id.append(i)
        predictions = algo.test(testset_id)
        for uid, iid, true_r, est, _ in predictions:
            # TODO: results는 user_id를 key로,  [(item_id, estimated_rating)의 tuple이 모인 list]를 value로 갖는 dictionary
            results[uid].append((iid, est))
    else:
        # TODO: testset의 데이터 중 item id가 id_list 안에 있는 데이터만 따로 testset_id라는 list로 저장
        # Hint: testset은 (user_id, item_id, default_rating)의 tuple을 요소로 갖는 list
        testset_id = []
        for i in testset:
            if i[1] in id_list:
                testset_id.append(i)
        predictions = algo.test(testset_id)
        for uid, iid, true_r, est, _ in predictions:
            # TODO - results는 item_id를 key로, [(user_id, estimated_rating)의 tuple이 모인 list]를 value로 갖는 dictionary(3점)
            results[iid].append((uid, est))
    for id_, ratings in results.items():
        # TODO: rating 순서대로 정렬하고 top-n개만 유지
        ratings.sort(key=lambda x: x[1], reverse=True)
        results[id_] = ratings[:n]
    
    return results


# PART 3. Requirement 3-2, 3-3, 3-4
def part3():
    file_path = 'DMA_project2_team%02d_part2_UIR.csv' % team
    reader = Reader(line_format='user item rating', sep=',', rating_scale=(1,10), skip_lines=1)
    data = Dataset.load_from_file(file_path, reader=reader)

    trainset = data.build_full_trainset()
    testset = trainset.build_anti_testset()

    # TODO: Requirement 3-2. User-based Recommendation
    uid_list = ['ffffbe8d854a4a5a8ab1a381224f5b80',
                'ffe2f26d5c174e13b565d026e1d8c503',
                'ffdccaff893246519b64d76c3561d8c7',
                'ffdb001850984ce69c5f91360ac16e9c',
                'ffca7b070c9d41e98eba01d23a920d52'] 
    # TODO - set algorithm for 3-2-1
    options = {'name' :'cosine', 'user_based': True}
    algo = surprise.KNNBasic(sim_options=options)
    algo.fit(trainset)
    results = get_top_n(algo, testset, uid_list, n=10, user_based=True)
    with open('3-2-1.txt', 'w') as f:
        for uid, ratings in sorted(results.items(), key=lambda x: x[0]):
            f.write('User ID %s top-10 results\n' % uid)
            for iid, score in ratings:
                f.write('Item ID %s\tscore %s\n' % (iid, str(score)))
            f.write('\n')
            

    # TODO - set algorithm for 3-2-2
    options = {'name' :'pearson', 'user_based': True}
    algo = surprise.KNNWithMeans(sim_options=options)
    algo.fit(trainset)
    results = get_top_n(algo, testset, uid_list, n=10, user_based=True)
    with open('3-2-2.txt', 'w') as f:
        for uid, ratings in sorted(results.items(), key=lambda x: x[0]):
            f.write('User ID %s top-10 results\n' % uid)
            for iid, score in ratings:
                f.write('Item ID %s\tscore %s\n' % (iid, str(score)))
            f.write('\n')

    # TODO - 3-2-3. Best Model
    options = {'name' :'pearson_baseline', 'user_based': True}
    best_algo_ub = surprise.KNNBaseline(sim_options=options)


    # TODO: Requirement 3-3. Item-based Recommendation
    iid_list = ['art', 'teaching', 'career', 'college', 'medicine']
    # TODO - set algorithm for 3-3-1
    options = {'name' :'cosine', 'user_based': False}
    algo = surprise.KNNBasic(sim_options=options)
    algo.fit(trainset)
    results = get_top_n(algo, testset, iid_list, n=10, user_based=False)
    with open('3-3-1.txt', 'w') as f:
        for iid, ratings in sorted(results.items(), key=lambda x: x[0]):
            f.write('Item ID %s top-10 results\n' % iid)
            for uid, score in ratings:
                f.write('User ID %s\tscore %s\n' % (uid, str(score)))
            f.write('\n')

    # TODO - set algorithm for 3-3-2
    options = {'name' :'pearson', 'user_based': False}
    algo = surprise.KNNWithMeans(sim_options=options)
    algo.fit(trainset)
    results = get_top_n(algo, testset, iid_list, n=10, user_based=False)
    with open('3-3-2.txt', 'w') as f:
        for iid, ratings in sorted(results.items(), key=lambda x: x[0]):
            f.write('Item ID %s top-10 results\n' % iid)
            for uid, score in ratings:
                f.write('User ID %s\tscore %s\n' % (uid, str(score)))
            f.write('\n')

    # TODO - 3-3-3. Best Model
    options = {'name' :'msd', 'user_based': False}
    best_algo_ub = surprise.KNNBaseline(sim_options=options)


    # TODO: Requirement 3-4. Matrix-factorization Recommendation
    # TODO - set algorithm for 3-4-1
    algo = surprise.SVD(n_factors=100, n_epochs=50, biased=False)
    algo.fit(trainset)
    results = get_top_n(algo, testset, uid_list, n=10, user_based=True)
    with open('3-4-1.txt', 'w') as f:
        for uid, ratings in sorted(results.items(), key=lambda x: x[0]):
            f.write('User ID %s top-10 results\n' % uid)
            for iid, score in ratings:
                f.write('Item ID %s\tscore %s\n' % (iid, str(score)))
            f.write('\n')

    # TODO - set algorithm for 3-4-2
    algo = surprise.SVD(n_factors=200, n_epochs=100, biased=True)
    algo.fit(trainset)
    results = get_top_n(algo, testset, uid_list, n=10, user_based=True)
    with open('3-4-2.txt', 'w') as f:
        for uid, ratings in sorted(results.items(), key=lambda x: x[0]):
            f.write('User ID %s top-10 results\n' % uid)
            for iid, score in ratings:
                f.write('Item ID %s\tscore %s\n' % (iid, str(score)))
            f.write('\n')

    # TODO - set algorithm for 3-4-3
    algo = surprise.SVDpp(n_factors=100, n_epochs=50)
    algo.fit(trainset)
    results = get_top_n(algo, testset, uid_list, n=10, user_based=True)
    with open('3-4-3.txt', 'w') as f:
        for uid, ratings in sorted(results.items(), key=lambda x: x[0]):
            f.write('User ID %s top-10 results\n' % uid)
            for iid, score in ratings:
                f.write('Item ID %s\tscore %s\n' % (iid, str(score)))
            f.write('\n')

    # TODO - set algorithm for 3-4-4
    algo = surprise.SVDpp(n_factors=100, n_epochs=100)
    algo.fit(trainset)
    results = get_top_n(algo, testset, uid_list, n=10, user_based=True)
    with open('3-4-4.txt', 'w') as f:
        for uid, ratings in sorted(results.items(), key=lambda x: x[0]):
            f.write('User ID %s top-10 results\n' % uid)
            for iid, score in ratings:
                f.write('Item ID %s\tscore %s\n' % (iid, str(score)))
            f.write('\n')

    # TODO - 3-4-5. Best Model
    best_algo_mf = surprise.SVDpp(n_factors=200, n_epochs=100)


##requirement3 베스트알고리즘 찾기
    ##매트릭스 베스트 알고리즘 찾기
    # benchmark = []
    #
    # for i, j in [(100, 50), (200, 100), (100, 100), (50, 50)]:
    #     kfold = KFold(n_splits=5, random_state=0)
    #     algorithm = SVD(n_factors=i, n_epochs=j, biased=False)
    #     results = cross_validate(algorithm, data, measures=['RMSE'], cv=kfold, verbose=False)
    #     tmp = pd.DataFrame.from_dict(results).mean(axis=0)
    #     tmp = tmp.append(pd.Series(
    #         ['SVD, ' + 'biased=False, ' + 'n_factors=' + str(i) + 'n_epochs=' + str(j).split(' ')[0].split('.')[-1]],
    #         index=['Algorithm']))
    #     benchmark.append(tmp)
    #     algorithm = SVD(n_factors=i, n_epochs=j, biased=True)
    #     results = cross_validate(algorithm, data, measures=['RMSE'], cv=kfold, verbose=False)
    #     tmp = pd.DataFrame.from_dict(results).mean(axis=0)
    #     tmp = tmp.append(pd.Series(
    #         ['SVD, ' + 'biased=True, ' + 'n_factors=' + str(i) + 'n_epochs=' + str(j).split(' ')[0].split('.')[-1]],
    #         index=['Algorithm']))
    #     benchmark.append(tmp)
    #     algorithm = SVDpp(n_factors=i, n_epochs=j)
    #     results = cross_validate(algorithm, data, measures=['RMSE'], cv=kfold, verbose=False)
    #     tmp = pd.DataFrame.from_dict(results).mean(axis=0)
    #     tmp = tmp.append(
    #         pd.Series(['SVD++, ' + 'n_factors=' + str(i) + 'n_epochs=' + str(j).split(' ')[0].split('.')[-1]],
    #                   index=['Algorithm']))
    #     benchmark.append(tmp)
    #     algorithm = NMF(n_factors=i, n_epochs=j, biased=True)
    #     results = cross_validate(algorithm, data, measures=['RMSE'], cv=kfold, verbose=False)
    #     tmp = pd.DataFrame.from_dict(results).mean(axis=0)
    #     tmp = tmp.append(pd.Series(
    #         ['NMF, ' + 'biased=True, ' + 'n_factors=' + str(i) + 'n_epochs=' + str(j).split(' ')[0].split('.')[-1]],
    #         index=['Algorithm']))
    #     benchmark.append(tmp)
    #     algorithm = NMF(n_factors=i, n_epochs=j, biased=False)
    #     results = cross_validate(algorithm, data, measures=['RMSE'], cv=kfold, verbose=False)
    #     tmp = pd.DataFrame.from_dict(results).mean(axis=0)
    #     tmp = tmp.append(pd.Series(
    #         ['NMF, ' + 'biased=False, ' + 'n_factors=' + str(i) + 'n_epochs=' + str(j).split(' ')[0].split('.')[-1]],
    #         index=['Algorithm']))
    #     benchmark.append(tmp)
    # pd.DataFrame(benchmark).set_index('Algorithm').sort_values('test_rmse')
    #
    #
    # ##유저 베스트 알고리즘 찾기
    # benchmark = []
    #
    # for algorithm in [KNNBasic, KNNWithMeans, KNNWithZScore, KNNBaseline]:
    #     for opt in ['cosine', 'pearson', 'msd', 'pearson_baseline']:
    #         name = str(algorithm).split('.')[-1].split("'")[0] + ', '
    #         options = {'name': opt, 'user_based': True}
    #         algo = algorithm(sim_options=options)
    #         kfold = KFold(n_splits=5, random_state=0)
    #
    #         results = cross_validate(algo, data, measures=['RMSE'], cv=kfold, verbose=False)
    #
    #         tmp = pd.DataFrame.from_dict(results).mean(axis=0)
    #         tmp = tmp.append(pd.Series([name + str(opt).split(' ')[0].split('.')[-1]], index=['Algorithm']))
    #         benchmark.append(tmp)
    # pd.DataFrame(benchmark).set_index('Algorithm').sort_values('test_rmse')
    #
    #
    # ##아이템 베스트 알고리즘 찾기
    #
    #
    # benchmark = []
    #
    # for algorithm in [KNNBasic, KNNWithMeans, KNNWithZScore, KNNBaseline]:
    #     name = str(algorithm).split('.')[-1].split("'")[0] + ', '
    #     for opt in ['cosine', 'pearson', 'msd', 'pearson_baseline']:
    #         options = {'name': opt, 'user_based': False}
    #         algo = algorithm(sim_options=options)
    #         kfold = KFold(n_splits=5, random_state=0)
    #
    #         results = cross_validate(algo, data, measures=['RMSE'], cv=kfold, verbose=False)
    #
    #         tmp = pd.DataFrame.from_dict(results).mean(axis=0)
    #         tmp = tmp.append(pd.Series([name + str(opt).split(' ')[0].split('.')[-1]], index=['Algorithm']))
    #         benchmark.append(tmp)
    # pd.DataFrame(benchmark).set_index('Algorithm').sort_values('test_rmse')

if __name__ == '__main__':
    part1()
    part2()
    part3()






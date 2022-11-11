import pyspark
from pyspark import SparkContext, SparkConf
from pyspark.sql import SparkSession
import pandas as pd
from pyspark.sql.functions import col,max,min,count,sum,avg,stddev_pop,hour,countDistinct,expr,stddev,window,column
from pyspark.ml.feature import RFormula
from pyspark.ml.classification import LogisticRegression
from pyspark.sql import Row, SparkSession, SQLContext
import math
import findspark

conf = SparkConf().setAppName('Question3')
#Local creates cluster in machine (* means all 16 cpu cores spark using, can put any number of cores)
#Put a Master IP if connecting to a cluster
conf = (conf.setMaster('local[*]') 
        .set('spark.executor.memory', '16G')
        .set('spark.driver.memory', '16G'))

#send configuation to context
sc = SparkContext(conf = conf)
Spark = SparkSession(sc)


t_df = Spark.read.csv("C:\\Users\\eschu\\OneDrive\\Documents\\EricsStuff\\DS503\\project2.3\\transactions.csv",inferSchema=True)

T1 = t_df.select(t_df["_c0"].alias("TransID"),
                 t_df["_c1"].alias("CustID"),
                 t_df["_c2"].alias("TransTotal"),
                 t_df["_c3"].alias("TransNumItems"),
                 t_df["_c4"].alias("TransDesc")).where(t_df["_c2"] >= 200)
T1.show()

T2 = T1.groupBy(T1["TransNumItems"].alias("Num_Items")).agg(sum("TransTotal").alias("SumTransTotal"), 
                                                  avg("TransTotal").alias("AvgTransTotal"), 
                                                  min("TransTotal").alias("MinTransTotal"), 
                                                  max("TransTotal").alias("MaxTransTotal"))
T2.show()

T3 = T1.groupBy(T1["CustID"].alias("CustID")).agg(count(T1["TransID"]).alias("TransactionsCount"))
T3.show()

T4 = t_df.select(t_df["_c0"].alias("TransID"),
                 t_df["_c1"].alias("CustID"),
                 t_df["_c2"].alias("TransTotal"),
                 t_df["_c3"].alias("TransNumItems"),
                 t_df["_c4"].alias("TransDesc")).where(t_df["_c2"] >= 600)
T4.show()

T5 = T4.groupBy(T4["CustID"].alias("CustID")).agg(count(T4["TransID"]).alias("TransactionsCount"))
T5.show()

#join does not return anything
T6 = T5.join(T3, T5["CustID"] == T3["CustID"]).select((T5["CustID"]), (T5["TransactionsCount"].alias("Count_T5")), 
                (T3["TransactionsCount"].alias("Count_T3"))).where((T5["TransactionsCount"] * 5) < T3["TransactionsCount"])
T6.show()





               
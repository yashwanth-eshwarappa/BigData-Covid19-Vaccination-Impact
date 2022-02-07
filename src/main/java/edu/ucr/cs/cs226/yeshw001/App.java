package edu.ucr.cs.cs226.yeshw001;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.StructType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.spark.sql.functions.*;

public class App
{

    public class Vaccine implements Serializable {
        String county;
        String countyType;
        String demographicCategory;
        String demographicValue;
        String estPopulation;
        String estAge12Plus;
        String estAge5Plus;
        String administeredDate;
        String partiallyVaccinated;
        String totalPartiallyVaccinated;
        String fullyVaccinated;
        String cumulativeFullyVaccinated;
        String atleastOneDose;
        String cumulativeAtleastOneDose;
        String cumulativeUnvaxTotal;
        String cumulativeUnvax12Plus;
        String cumulativeUnvax5Plus;
        String suppressData;
    }
    public class CovidCase implements Serializable {
        String caseMonth;
        String resState;
        String stateFipsCode;
        String resCounty;
        String countyFipsCode;
        String ageGroup;
        String sex;
        String race;
        String ethinicity;
        String casePositiveInterval;
        String caseOnsetInterval;
        String process;
        String exposure;
        String currentStatus;
        String symptomStatus;
        String hospital;
        String icu;
        String death;
        String underlyingConditions;
    }

    private static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main( String[] args ) throws IOException {

        String casesFilename = args[0];
        String vaccineFilename = args[1];

        SparkConf conf = new SparkConf().setAppName("test").setMaster("local").set("spark.testing.memory", "2147480000");
        JavaSparkContext sc = new JavaSparkContext(conf);

        SparkSession spark = SparkSession.builder()
                .master("local")
                .appName("test")
                .getOrCreate();

        //spark.sparkContext().setLogLevel("ERROR");

        StructType casesSchema = new StructType()
                .add("case_month", "string")
                .add("res_state", "string")
                .add("state_fips_code", "string")
                .add("res_county", "string")
                .add("county_fips_code", "string")
                .add("age_group", "string")
                .add("sex", "string")
                .add("race", "string")
                .add("ethnicity", "string")
                .add("case_positive_specimen_interval", "string")
                .add("case_onset_interval", "string")
                .add("process", "string")
                .add("exposure_yn", "string")
                .add("current_status", "string")
                .add("symptom_status", "string")
                .add("hospital_yn", "string")
                .add("icu_yn", "string")
                .add("death_yn", "string")
                .add("underlying_conditions_yn", "string");

        StructType vaccinesSchema = new StructType()
                .add("county", "string")
                .add("county_type", "string")
                .add("demographic_category", "string")
                .add("demographic_value", "string")
                .add("est_population", "string")
                .add("est_age_12plus_pop", "string")
                .add("est_age_5plus_pop", "string")
                .add("administered_date", "string")
                .add("partially_vaccinated", "string")
                .add("total_partially_vaccinated", "string")
                .add("fully_vaccinated", "string")
                .add("cumulative_fully_vaccinated", "string")
                .add("at_least_one_dose", "string")
                .add("cumulative_at_least_one_dose", "string")
                .add("cumulative_unvax_total_pop", "string")
                .add("cumulative_unvax_12plus_pop", "string")
                .add("cumulative_unvax_5plus_pop", "string")
                .add("administered_month", "string");

        Dataset<Row> dfCases = spark.read()
                .option("mode", "DROPMALFORMED")
                .schema(casesSchema)
                .csv(casesFilename);

        Dataset<Row> dfVaccination = spark.read()
                .option("mode", "DROPMALFORMED")
                .schema(vaccinesSchema)
                .csv(vaccineFilename);

        List<String> vaccineCumulativeCountDates= new ArrayList<String>(Arrays.asList("2020-07-31","2020-08-31", "2020-09-30",
                "2020-10-31","2020-11-30", "2020-12-31","2021-01-31","2021-02-28","2021-03-31", "2021-04-30","2021-05-31",
                "2021-06-30","2021-07-31", "2021-08-31", "2021-09-30", "2021-10-31", "2021-11-30","2021-12-31"));

        HashMap<String,List<List<String>>> lineChartMapAge=new HashMap<>();
        Set<String> distinctCounties=new HashSet<>();
        List<Row> countyList = dfVaccination.select("county").distinct().toDF().collectAsList();
        for(Row r: countyList)
        {
            distinctCounties.add(r.getString(0));
        }

        //Vaccine efficacy across various age based cohorts

        for(String county:distinctCounties)
        {
            Dataset<Row> vaccineAgeDF =dfVaccination.filter("demographic_category == 'Age Group'")
                    .filter("county =='"+county+"'")
                    .select("county","cumulative_fully_vaccinated","administered_date","administered_month","demographic_category","demographic_value")
                    .filter(dfVaccination.col("administered_date").isInCollection(vaccineCumulativeCountDates))
                    .sort(dfVaccination.col("administered_month").desc())
                    .groupBy("county","demographic_category","demographic_value")
                    .df();

            System.out.println("VaccineAgeGroupWiseAggregatedDataFrame sample");
            System.out.println(String.valueOf(vaccineAgeDF.getRows(20,100)));

            Dataset<Row> caseAgeDF = dfCases.select("res_county","case_month","age_group")
                    .filter("res_state=='CA'")
                    .filter("res_county =='"+county+"'")
                    .groupBy("res_county","case_month","age_group")
                    .count()
                    .sort("case_month")
                    .toDF();

            System.out.println("CasesAgeGroupWiseAggregatedDataFrame sample");
            System.out.println(String.valueOf(caseAgeDF.getRows(20,100)));

            Dataset<Row> joinedAgeDF=caseAgeDF
                    .join(vaccineAgeDF)
                    .where(caseAgeDF.col("case_month").equalTo(vaccineAgeDF.col("administered_month"))
                            .and(caseAgeDF.col("res_county").equalTo(vaccineAgeDF.col("county")))
                            .and(caseAgeDF.col("age_group").equalTo(vaccineAgeDF.col("demographic_value"))))
                    .sort(col("case_month").desc())
                    .select("res_county","case_month","age_group","count","cumulative_fully_vaccinated");

            System.out.println("AgeGroupWiseAggregatedDataFrame - joined DF - sample");
            System.out.println(String.valueOf(joinedAgeDF.getRows(20,100)));

            List<List<String>> lineItem=new ArrayList<>();
            List<Row> lineChartRow=joinedAgeDF.toDF().collectAsList();
            Set<String> distinctAgeGroup=new HashSet<>();
            for(Row r:lineChartRow)
            {
                List<String> l=new ArrayList<>();
                l.add(r.getString(0));
                l.add(r.getString(1));
                l.add(r.getString(2));
                l.add(String.valueOf(r.getLong(3)));
                l.add(r.getString(4));
                distinctAgeGroup.add(r.getString(2));
                lineItem.add(l);
            }
            for(String s:distinctAgeGroup)
            {
                List<List<String>> mapList = new ArrayList<>();
                List<List<String>> t= lineItem.stream().filter(c->c.get(2).equals(s)).collect(Collectors.toList());
                for(List<String> el:t)
                {
                    el.remove(0);
                    el.remove(1);
                    mapList.add(el);
                }
                lineChartMapAge.put(county+"_"+s,mapList);
            }

            System.out.println("Input map for LineChartGenerator: AgeWiseAggregatedData");
            System.out.println(lineChartMapAge.toString());

            for(HashMap.Entry<String,List<List<String>>> e:lineChartMapAge.entrySet())
            {
                System.out.println("Generating charts for: "+e.getKey());
                LineChart lineChart =  new LineChart(e.getKey().split("_")[0],e.getKey(), e.getValue());
            }
            System.out.println("Calculating Pearson and Spearman Correlation Coefficients across age group cohorts");
            calculateCorrelationCoefficients(joinedAgeDF);
            lineChartMapAge.clear();
        }

        //Vaccine efficacy across various race cohorts

        HashMap<String,List<List<String>>> lineChartMapRace=new HashMap<>();

        for(String county:distinctCounties)
        {
            Dataset<Row> vaccineRaceDF =dfVaccination.filter("demographic_category == 'Race/Ethnicity'")
                    .filter("county =='"+county+"'")
                    .select("county","cumulative_fully_vaccinated","administered_date","administered_month","demographic_category","demographic_value")
                    .filter(dfVaccination.col("administered_date").isInCollection(vaccineCumulativeCountDates))
                    .sort(dfVaccination.col("administered_month").desc())
                    .groupBy("county","demographic_category","demographic_value")
                    .df();


            System.out.println(vaccineRaceDF.describe().toString());
            System.out.println("Schema of vaccine data across race cohorts");
            vaccineRaceDF.printSchema();

            System.out.println("VaccineRaceWiseAggregatedDataFrame");
            System.out.println(vaccineRaceDF.getRows(20,100));

            Dataset<Row> caseRaceDF = dfCases.select("res_county","case_month","race")
                    .filter("res_state=='CA'")
                    .filter("res_county =='"+county+"'")
                    .groupBy("res_county","case_month","race")
                    .count()
                    .sort("case_month")
                    .toDF();

            System.out.println("CasesRaceWiseAggregatedDataFrame");
            System.out.println(caseRaceDF.getRows(20,100));
            System.out.println("Schema of case data across race cohorts");
            caseRaceDF.printSchema();

            Dataset<Row> joinedRaceDF=caseRaceDF
                    .join(vaccineRaceDF)
                    .where(caseRaceDF.col("case_month").equalTo(vaccineRaceDF.col("administered_month"))
                            .and(caseRaceDF.col("res_county").equalTo(vaccineRaceDF.col("county")))
                            .and(caseRaceDF.col("race").equalTo(vaccineRaceDF.col("demographic_value"))))
                    .sort(col("case_month").desc())
                    .select("res_county","case_month","race","count","cumulative_fully_vaccinated");


            System.out.println("RaceWiseAggregatedDataFrame - joined DF - sample");
            System.out.println(joinedRaceDF.getRows(50,100));
            joinedRaceDF.describe();
            joinedRaceDF.printSchema();

            List<List<String>> lineitem=new ArrayList<>();
            List<Row> lineChartRow=joinedRaceDF.toDF().collectAsList();
            Set<String> distinctRaceGroup=new HashSet<>();
            for(Row r:lineChartRow)
            {
                List<String> l=new ArrayList<>();
                l.add(r.getString(0));
                l.add(r.getString(1));
                l.add(r.getString(2));
                l.add(String.valueOf(r.getLong(3)));
                l.add(r.getString(4));
                distinctRaceGroup.add(r.getString(2));
                lineitem.add(l);
            }
            for(String s:distinctRaceGroup)
            {
                List<List<String>> mapList = new ArrayList<>();
                List<List<String>> t= lineitem.stream().filter(c->c.get(2).equals(s)).collect(Collectors.toList());
                for(List<String> el:t)
                {
                    el.remove(0);
                    el.remove(1);
                    mapList.add(el);
                }
                lineChartMapRace.put(county+"_"+s,mapList);
            }

            for(HashMap.Entry<String,List<List<String>>> e:lineChartMapRace.entrySet())
            {
                System.out.println("Generating charts for: "+e.getKey());
                LineChart lineChart =  new LineChart(county,e.getKey(), e.getValue());
                System.out.println(e.getKey());
            }
            lineChartMapRace.clear();

            System.out.println("Calculating Pearson and Spearman Correlation Coefficients across race cohorts:");
            calculateCorrelationCoefficients(joinedRaceDF);
        }
    }

    private static void calculateCorrelationCoefficients(Dataset<Row> joinedDF) {
        List<Row> l1= joinedDF.select("count").collectAsList();
        List<Row> l2= joinedDF.select("cumulative_fully_vaccinated").collectAsList();
        List<Double> ll1=new ArrayList<>();
        for(Row r:l1) {
            ll1.add((double) r.getLong(0));
        }

        List<Double> ll2=new ArrayList<>();
        for(Row r:l2) {
            ll2.add(Double.parseDouble(r.getString(0)));
        }

        double[] ld1 =ll1.stream().mapToDouble(Double::doubleValue).toArray();
        double[] ld2 =ll2.stream().mapToDouble(Double::doubleValue).toArray();

        for(int i=1;i<ld1.length;i++)
        {
            ld1[i]+=ld1[i-1];
        }

        Double coef=new PearsonsCorrelation().correlation(ld1,ld2);
        System.out.println("Pearson Correlation coefficient: "+ coef);

        Double spearman=new SpearmansCorrelation().correlation(ld1,ld2);
        System.out.println("Spearman Correlation coefficient: "+ spearman);
    }
}

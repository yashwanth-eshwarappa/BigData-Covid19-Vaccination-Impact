# Covid19-Vaccination-Impact
The objective of the project is to gather, process and analyze publicly available COVID related data acquired from various reliable sources such as CDC and JHU. The application finds strong correlation between the features such as number of cases across different cohorts and how it affects COVID case count in that particular geography, over time. The results are accessed using Pearson’s and Spearman’s correlation coefficient statistical methods to visualize and advocate for the effectiveness of the vaccine and hopefully further assuage vaccination hesitancy and promote vaccination.

THE BIG DATA PROBLEM:
Massive amounts of COVID-19 related data is available from across the globe for various aspects such as case rate, fatality rate, vaccination rate. These datasets are growing fast with every passing day. With more granularity being included such as county, zip code, the dataset size further gets multiplied by that factor. These datasets characterize the 4 V’s of Big Data ie., Volume, Velocity, Veracity, Variety.
Traditional data analysis methods are rendered moot on such a large scale dataset. To overcome this challenge we plan to leverage big data analysis techniques to analyse these large scale datasets in an efficient manner.

DATA PREPROCESSING:
All the null values were filled with appropriate replacement without altering the characteristics of the data, this was highly challenging as the number of rows with null values proved to be significantly higher than we anticipated. We had multiple datasets with various columns having null which all needed to be filled or dropped, All the datasets had different columns and column types. In order to join datasets we had to make them compatible with each other in order to join and perform our analysis.We hence make appropriate feature extractions, perform encoding and bucketing in order to normalize the data across all datasets.

CORRELATION:
Correlation is a statistical method used to assess a possible linear association between two continuous variables.  It is formally defined as a reciprocal relation between two or more things; a statistic representing how closely two variables co-vary; it can vary from -1 (perfect negative correlation) through 0 (no correlation) to +1 (perfect positive correlation). There are two main kinds of correlation coefficient which are widely used. In our literature survey we found this to be a good metric for comparing our data and we have used the following:

Pearson’s product moment correlation coefficient:
Pearson’s product moment correlation coefficient is denoted as ρ for a population parameter and as r for a sample statistic. It is used when both variables being studied are normally distributed. This coefficient is affected by extreme values, which may exaggerate or dampen the strength of the relationship, and is therefore inappropriate when either or both variables are not normally distributed.

Spearman’s rank correlation coefficient 
Spearman’s rank correlation coefficient is denoted as ρs for a population parameter and as rs for a sample statistic. It is appropriate when one or both variables are skewed or ordinal and is robust when extreme values are present. 

RESULTS AND VISUALIZATION:
The most effective way to communicate with an audience especially statistically is through visualisation. We have performed various visualisation techniques.

This is the data for some of the counties and the correlation coefficient which was calculated. We can clearly see that the correlation suggests that the vaccine is highly effective in proving the hypothesis we assumed.

Pearson correlation:
County 
Age Group
Race 
Riverside 
-0.6712249132524414
-0.3771182327713697
San Diego 
 -0.6611590077895735
-0.41523425495170335
Alameda
 -0.6456493805583197
-0.48890954537814735

Spearman Correlation:
County 
Age Group
Race 
Riverside 
-0.9118434803022837
 -0.8520509543362651
San Diego 
 -0.9000623110859864
 -0.8364895579418875
Alameda
-0.8968530803431033
-0.8615849204262467


Example:
In the county of ALAMEDA across various age groups we can clearly see trends across various age groups of 0-17 and 18-49 respectively.
![N6aBID1W8zq1i_9bG8COQW4gfmmoAIwSNimVlBp2lABXPH1Z4nzeyr5--jyTXRnTIXpOVOyRn-XAuJov5mjYZcDx3sErV02QiwiJIdrlC8gz1Yes8rV0-j25QOEF](https://user-images.githubusercontent.com/97561730/149031188-e229f522-aeda-4750-a32c-72baa81dceb6.jpg)
Fig 1. Correlation between vaccination and case counts for Alameda county for the age group 0-17

![VrpUPqPWPweOisi2LT2CX-tx0EQYJPiU6k8Obhj1bIs59Xiy2cG3Yky2esxopPfl1UEn_UlE6Uk85_Ph2Ivm5ZYrnHErMZp1Qz03gMoSfZOCisOPrrLElccAeiCZ](https://user-images.githubusercontent.com/97561730/149031268-aef49d6b-4718-4236-bce3-bbc267bf5e3b.jpg)
Fig 2. Correlation between vaccination and case counts for Alameda county for the age group 18-49

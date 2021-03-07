# momomanager

!!! Work in progress, can change anytime

## Quick links
- [How to use the library](#use_lib)
- [API Documentation](https://github.com/Zakaria16/momomanager/tree/master/app/momomanager/doc)

### Overview
Easily read mobile money transaction records and detect Momo fraud messages

The app allows you to know your current balance, the total amount you have received so far and the total amount you have spent.

It groups your transaction into categories

* Received transactions
* Sent Transactions
* Airtime Transactions
* It detect incoming SMS to verify if it is a legit Mobile Money message


### current balance and all time Money received and spent
Get to know your current balance and the total amount you have spent and received thus far

### Detect fraud Momo messages
detect whether incoming mobile money message is legit and alert you. The app only show Momo transactions from the network operator if its not from the operator is not shown

### All Transactions
At this section you get to see all your transactions beautifully differentiated with colors

### Received Transactions
This category shows all mobile money transaction you have received so far. you easily read the amount, the transaction ID and the date it was received

### Spent Transactions
This category shows all mobile money transaction you have sent from your device so far. you easily read the amount, the transaction ID and the date it was sent

### Airtime Transactions
At this category you will know the amount you are spending on airtime


## <a name="use_lib"></a> How to use the library
add the library to your project
```
dependencies {
    ...
    implementation 'com.mazitekgh:momomanager:1.0.0'
}
```

### using the library

```java
MtnMomoManager mtnMomoManager = new MtnMomoManager(context);
//get the sum of all received momo amount
double receivedAmount = mtnMomoManager.getTotalReceivedAmount();
//get the sum of all sent momo amount
double totalSent = mtnMomoManager.getTotalSentAmount();
//get the current momo balance
double currentBalance = mtnMomoManager.getLatestBalance();

List<Momo> resList;
//list of all momo data
resList = mtnMomoManager.getMomoData(ExtractMtnMomoInfo.ALL_MOMO);
//list of all received momo data
resList = mtnMomoManager.getMomoData(ExtractMtnMomoInfo.RECEIVED_MOMO);
//list of all sent momo data
resList = mtnMomoManager.getMomoData(ExtractMtnMomoInfo.SENT_MOMO);

//list of all momo used to buy airtime
resList = mtnMomoManager.getMomoData(ExtractMtnMomoInfo.CREDIT_MOMO);

```

Read the API Documentation here: [API Doc](https://github.com/Zakaria16/momomanager/tree/master/app/momomanager/doc)

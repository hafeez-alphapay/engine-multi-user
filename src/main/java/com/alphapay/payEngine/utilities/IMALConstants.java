package com.alphapay.payEngine.utilities;

public class IMALConstants {
    public static final String ACCOUNTS_ARRAY_TAG="accountDC";
    public static final String CIF_DETAIL_MOBILE="mobile";
    public static final String CIF_DETAIL_EMAIL="email";

    public static final String ACCOUNT_TYPE_IDENTIFICATION_CODE="accGl";

    public static final String CIF_DETAIL_TELEPHONE="telephone";
    public static final String CIF_DETAIL_NAME_AR="fullNameArabic";
    public static final String CIF_DETAIL_NAME_AR2="longNameArabic";
    public static final String CIF_DETAIL_NAME_EN="fullNameEnglish";
    public static final String ACCOUNT_GL="accGl";
    public static final String CIF_DETAIL_NAME_EN2="longNameEnglish";
    public static final String CIF_DETAIL_DATE_OF_BIRTH="dateOfBirth";
    public static final String CIF_DETAIL_GENDER="gender";
    public static final String CIF_DETAIL_PERMANENT_ADDRESS="permanentAddress";
    public static final String ACC_NUMBER="accountNumber";
    public static final String ACCOUNT_TYPE="generalLedgerBriefNameEng";
    public static final String ACCOUNT_TYPE_AR="generalLedgerBriefNameArabic";
    public static final String ADD_REFERENCE="additionalRef";
    public static final String IBAN="ibanAccNo";

    public static final String STATEMENT_ARRAY_TAG="accountDetail";
    public static final String OPERATION_SIGN_TAG="amountCreditOrDebit";
    public static final String BASE_CURRENCY_BALANCE="balanceBaseCurrency";

    public static final String BASE_CURRENCY_AMOUNT="amountBaseCurrency";

    public static final String FR_CURRENCY_BALANCE="balanceForeignCurrency";

    public static final String FR_CURRENCY_AMOUNT="amountForeignCurrency";

    public static final String ACC_CURRENCY="currency";
    public static final String ACC_CURRENCY_AR="currencyAr";


    public static final String DESCRIPTION_TAG="description";
    public static final String JVC_REF_TAG="jvReference";
    public static final String POST_DATE_TAG="postdate";
    public static final String TIME_AUTHORIZED_TAG="timeAuthorized";
    public static final String SIDX_TAG="sidx";
    public static final String OPERATION_NUM_TAG="operationNumber";
    public static final String TRADE_DATE_TAG="tradeDate";

    public static final String REVERSED_FLAG="reversalFlag";
    public static final String TRANSACTION_NUMBER_TAG="transactionNumber";
    public static final String VALUE_DATE="valueDate";

    public static final String CIF_STATUS="status";
    public static final String CIF_TYPE="type";

    public static final String MOCK_MODE="MOCK";
    public static final String DEFAULT_CURRENCY="SDG";

    public static final String CHEQUE_NUMBER = "cheqNumber";
    public static final String CHEQUE_SERIAL = "cheqSerial";
    public static final String CHEQUE_STATUS = "cheqStatus";
    public static final String CHEQUE_ISSUE_DATE = "issueDate";
    public static final String CHEQUE_CLEAR_DATE = "clearDate";
    public static final String BRABCH_EN = "branchNameEn";
    public static final String BRABCH_AR = "branchNameAr";

    public static final String INSTALLMENT_CURRENCY = "currencyCode";
    public static final String INSTALLMENTS_ARRAY_TAG = "statment" ;
    public static final String INSTALLMENT_AMOUNT = "instlAmount";
    public static final String INSTALLMENT_NUMBER = "instlNumber";
    public static final String INSTALLMENT_STATUS = "instlStatus";
    public static final String INSTALLMENT_DATE ="instlDate" ;

    public static final String RATES_ARRAY_TAG = "exchangeRates";
    public static final String EXCHANGE_VALUE = "exchgValue";
    public static final String EXCHANGE_FROM_CURR ="fromCurrency" ;
    public static final String EXCHANGE_TO_CURR = "toCurrency" ;
    public static final String EXCHANGE_RATE_TYPE = "rateType";
    public static final String EXCHANGE_DATE = "exchgDate";
    public static final String ACCOUNT_TYPE_ID = "accountType";


    public static boolean simpleAKBCorportaeCheck(String cif)
    {
         return  (cif!=null && cif.startsWith("2"))?true:false;
    }
    public static final String PUBLIC_KEY = "publicKey";
}

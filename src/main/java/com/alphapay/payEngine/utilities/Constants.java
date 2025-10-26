package com.alphapay.payEngine.utilities;

public class Constants {

    public static final Integer PENDING_TRANSACTION_CODE = 101;
    public static final String PENDING_TRANSACTION_MESSAGE = "Payment Pending";

    public static final Integer SUCCESS_TRANSACTION_CODE = 1;
    public static final String SUCCESS_TRANSACTION_MESSAGE = "Successful";

    public static final String DB_ERROR_MESSAGE = "Database error";
    public static final Integer DB_ERROR_CODE = 601;

    public static final String SESSION_INITIATION_ERROR_MESSAGE = "Failed to initiate new session";
    public static final Integer SESSION_INITIATION_ERROR_CODE = 602;

    public static final String UPDATE_SESSION_ERROR_MESSAGE = "Failed to update session";
    public static final Integer UPDATE_SESSION_ERROR_CODE = 603;

    public static final String EXECUTE_PAYMENT_ERROR_MESSAGE = "Failed to execute payment";
    public static final Integer EXECUTE_PAYMENT_ERROR_CODE = 604;

    public static final String PROCESS_SESSION_ERROR_MESSAGE = "Failed to process the intiated session";
    public static final Integer PROCESS_SESSION_ERROR_CODE = 605;

    public static final Integer CARD_ALREADY_SAVED_CODE = 606;
    public static final String CARD_ALREADY_SAVED_MESSAGE = "Card already saved";

    public static final Integer ALIAS_ALREADY_EXISTS_CODE = 607;
    public static final String ALIAS_ALREADY_EXISTS_MESSAGE = "Alias already exists";

    public static final Integer CARD_NOT_FOUND_CODE = 608;
    public static final String CARD_NOT_FOUND_MESSAGE = "Card not found";

    public static final Integer BILLER_RESPONSE_TIMEOUT_CODE = 502;
    public static final String BILLER_RESPONSE_TIMEOUT_STATUS = "Failed";
    public static final String BILLER_RESPONSE_TIMEOUT_MESSAGE = "BILLER_RESPONSE_TIMEOUT";


    public static final Integer BILLER_CONNECT_TIMEOUT_CODE = 500;
    public static final String BILLER_CONNECT_TIMEOUT_STATUS = "Failed";
    public static final String BILLER_CONNECT_TIMEOUT_MESSAGE = "BILLER_CONNECTION_ERROR";

    public static final Integer INPUT_OUTPUT_PROCESSING_ERROR_CODE = 500;
    public static final String INPUT_OUTPUT_PROCESSING_ERROR_CODE_STATUS = "Failed";
    public static final String  INPUT_OUTPUT_PROCESSING_ERROR_CODE_MESSAGE = "IO ERROR WHILE PROCESSING TRANSACTION";

    public static final Integer INSUFFICIENT_FUNDS_CODE = 901;
    public static final String INSUFFICIENT_FUNDS_STATUS = "Failed";
    public static final String INSUFFICIENT_FUNDS_MESSAGE = "Insufficient Funds";

    public static final Integer OPEN_CIRCUIT_CODE = 902;
    public static final String OPEN_CIRCUIT_STATUS = "Failed";
    public static final String OPEN_CIRCUIT_MESSAGE = "OPEN CIRCUIT DUE TO PREVIOUS FAILURES";

    public static final Integer GW_VALIDATION_UNAUTHORIZED_CODE = 400;
    public static final String GW_VALIDATION_UNAUTHORIZED_MESSAGE = "Unauthorized GW request";


    public static final Integer VALIDATION_INVALID_REQUEST_TYPE_ACTION_DECLINE_CODE = 401;
    public static final String VALIDATION_INVALID_REQUEST_TYPE_ACTION_DECLINE_MESSAGE = "INTERNAL PROCESSING ERROR IN GW";

    public static final Integer VALIDATION_INVALID_SERIAL_ID_DECLINE_CODE = 402;
    public static final String VALIDATION_INVALID_SERIAL_ID_DECLINE_MESSAGE = "Invalid Serial ID, Please check service config (external biller id)";

    public static final Integer VALIDATION_INVALID_REQUEST_TYPE_CODE = 403;
    public static final String VALIDATION_INVALID_REQUEST_TYPE_MESSAGE = "Request type (credit or precheck) is not configured for this service";


    public static final Integer NOT_FOUND_CODE = 404;
    public static final String VALIDATION_INVALID_CONFIG_MESSAGE = "Service Config in Agg is not matching biller services";

    public static final Integer VALIDATION_INVALID_AMOUNT_CODE = 405;
    public static final String VALIDATION_INVALID_AMOUNT_MESSAGE = "Invalid Amount";


    public static final Integer VALIDATION_INVALID_FEE_CODE = 406;
    public static final String VALIDATION_INVALID_FEE_MESSAGE = "Fee is less than biller fees";

    public static final Integer TRANSACTION_NOT_FOUND_CODE = 36;
    public static final String TRANSACTION_NOT_FOUND_CODE_MESSAGE = "No record found";

    public static final Integer TRANSACTION_FAILED_CODE = 33;
    public static final String TRANSACTION_FAILED_MESSAGE = "Failed";

    public static final Integer GW_VALIDATION_INVALID_REQUEST_TYPE_DECLINE_CODE = 401;
    public static final String GW_VALIDATION_INVALID_REQUEST_TYPE_DECLINE_MESSAGE = "INTERNAL PROCESSING ERROR IN GW";

    public static final String DEFAULT_CURRENCY = "AED";

    public static final String DEFAULT_CURRENCY_CODE = "40";

    public static final String FAILURE_STATUS = "Failed";

    public static final String SUCCESS_CODE = "200";

    public static final String Not_FOUND_CODE = "404";

    public static final String TOKEN_PREFIX = "Tokenized-";

    public static final String CARD_TOKEN_TYPE = "TokenizedCard";

    public static final String TOKEN_TYPE = "mftoken";

    public static final String AGGREGAOR_SELFTOPUP = "SELFTOPUP";
    public static final String AGGREGAOR_BILLPAYMENT = "BILLPAYMENT";

    public class MYFATOORAH_ENDPOINTS
    {
        public static final String INITIATE_SESSION = "InitiateSession";
        public static final String UPDATE_SESSION = "UpdateSession";
        public static final String EXECUTE_PAYMENT = "ExecutePayment";
        public static final String SAVE_CARD = "SaveCard";
    }

    public class CALLBACK_EVENTS
    {
        public static final String STATUS_CHANGED = "TransactionsStatusChanged";
    }
}

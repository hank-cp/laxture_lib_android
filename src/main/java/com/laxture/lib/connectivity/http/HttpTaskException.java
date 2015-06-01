package com.laxture.lib.connectivity.http;

import com.laxture.lib.Configuration;
import com.laxture.lib.R;
import com.laxture.lib.task.TaskException;

public class HttpTaskException extends TaskException {

    private static final long serialVersionUID = 2465598838810874933L;

    public static final int HTTP_ERR_CODE_SUCCESSFUL = ERROR_CODE_SUCCESSFUL;

    public static final int HTTP_ERR_CODE_NETWORK_NOT_AVAILABLE = 90001;

    public static final int HTTP_ERR_CODE_CONNECTION_ERROR = 90002;

    public static final int HTTP_ERR_CODE_CONNECTION_TIME_OUT = 90003;

    public static final int HTTP_ERR_CODE_SERVER_ERROR = 90004;

    public static final int HTTP_ERR_CODE_DOWNLOAD_ERROR = 90005;

    public static final int HTTP_ERR_CODE_SAVE_DOWNLOAD_ERROR = 90006;

    public static final int HTTP_ERR_CODE_UNKNOW_ERROR = 90100;

    public HttpTaskException(int errorCode, Throwable throwable) {
        super(errorCode, getErrorMessage(errorCode), throwable);
    }

    public HttpTaskException(int errorCode) {
        super(errorCode, getErrorMessage(errorCode));
    }

    public static String getErrorMessage(int errorCode) {
        switch (errorCode) {
        case HTTP_ERR_CODE_NETWORK_NOT_AVAILABLE:
            return Configuration.getInstance().getAppContext().getString(
                    R.string.msg_http_err_network_not_available);

        case HTTP_ERR_CODE_CONNECTION_ERROR:
            return Configuration.getInstance().getAppContext().getString(
                    R.string.msg_http_err_connection_error);

        case HTTP_ERR_CODE_CONNECTION_TIME_OUT:
            return Configuration.getInstance().getAppContext().getString(
                    R.string.msg_http_err_connection_time_out);

        case HTTP_ERR_CODE_SERVER_ERROR:
            return Configuration.getInstance().getAppContext().getString(
                    R.string.msg_http_err_server_error);

        case HTTP_ERR_CODE_DOWNLOAD_ERROR:
            return Configuration.getInstance().getAppContext().getString(
                    R.string.msg_http_err_download_error);

        case HTTP_ERR_CODE_SAVE_DOWNLOAD_ERROR:
            return Configuration.getInstance().getAppContext().getString(
                    R.string.msg_http_err_save_download_error);

        case HTTP_ERR_CODE_UNKNOW_ERROR:
            return Configuration.getInstance().getAppContext().getString(
                    R.string.msg_http_err_network_not_available);

        default:
            // status code errors, e.g. 404, 403, 500
            return errorCode != 0 ?
                Configuration.getInstance().getAppContext().getString(
                    R.string.msg_http_err_server_error) : "";
        }
    }

}

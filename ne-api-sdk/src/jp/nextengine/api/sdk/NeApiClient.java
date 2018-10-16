/**
 * Nextengine API SDK(http://api.next-e.jp/).
 *
 * @since 2013/11/01
 * @copyright Hamee Corp. All Rights Reserved.
 * @author Hamee Corp.
 */
package jp.nextengine.api.sdk;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jp.nextengine.api.sdk.NeApiClientException;
import java.net.HttpURLConnection;
import java.io.PrintWriter;

/**
 *
 * @author Sys
 */
public class NeApiClient {

    ////////////////////////////////////////////////////////////////////////////
    // ���p����T�[�o�[��URL�̃X�L�[���{�z�X�g���̒�`
    ////////////////////////////////////////////////////////////////////////////
    public static final String SERVER_HOST_API = "https://api.next-engine.org";
    public static final String SERVER_HOST_NE  = "https://base.next-engine.org";

    ////////////////////////////////////////////////////////////////////////////
    // �F�؂ɗp����URL�̃p�X���`
    ////////////////////////////////////////////////////////////////////////////
    public static final String PATH_LOGIN = "/users/sign_in/";	// NE���O�C��
    public static final String PATH_OAUTH = "/api_neauth/";	// API�F��    

    ////////////////////////////////////////////////////////////////////////////
    // API�̃��X�|���X�̏������ʃX�e�[�^�X�̒�`
    ////////////////////////////////////////////////////////////////////////////
    public static final String RESULT_SUCCESS = "success";        // ����
    public static final String RESULT_ERROR = "error";            // ���s
    public static final String RESULT_REDIRECT = "redirect";      // �v���_�C���N�g

    ////////////////////////////////////////////////////////////////////////////
    // Oauth�p�����[�^�̒�`
    ////////////////////////////////////////////////////////////////////////////
    public static final String KEY_RESULT = "result";              // ����
    public static final String KEY_CODE = "code";                  // �G���[�R�[�h
    public static final String KEY_MESSAGE = "message";            // ���b�Z�[�W
    public static final String KEY_CLIENT_ID = "client_id";        // �N���C�A���gID
    public static final String KEY_CLIENT_SECRET = "client_secret";// �N���C�A���g�V�[�N���b�g
    public static final String KEY_REDIRECT_URI = "redirect_uri";  // ���_�C���N�gURI
    public static final String KEY_UID = "uid";                    // uid(���[�U�[�ŗL�̎���ID)
    public static final String KEY_STATE = "state";                // state(�L������)
    public static final String KEY_ACCESS_TOKEN = "access_token";  // �A�N�Z�X�g�[�N��
    public static final String KEY_REFRESH_TOKEN = "refresh_token";// ���t���b�V���g�[�N��

    ///////////////////////////////////////////////////////
    // SDK������API�𗘗p����ׂɎg�������o�ϐ�
    ///////////////////////////////////////////////////////
    // OAuth�F�؂̃p�����[�^
    protected String _client_id = null;
    protected String _client_secret = null;
    protected String _redirect_uri = null;
    protected String _uid = null;
    protected String _state = null;
    protected String _access_token = null;
    protected String _refresh_token = null;

    // ���N�G�X�g
    private HttpServletRequest _request = null;
    // ���X�|���X
    private HttpServletResponse _response = null;
    // Web�A�v���P�[�V������(false:�o�b�`���̔񓯊�����)
    private boolean _is_web = true;

    public String getAccessToken() {
        return _access_token;
    }

    public String getRefreshToken() {
        return _refresh_token;
    }

    /**
     * �ʏ��Web�A�v���P�[�V�����̏ꍇ(������API�����s����ꍇ)�A�{�R���X�g���N�^���ĂуC���X�^���X�𐶐����ĉ������B
     *
     * redirect_uri�̐����F �܂��F�؂��Ă��Ȃ����[�U�[���A�N�Z�X�����ꍇ(�l�N�X�g�G���W�����O�C�����K�v�ȏꍇ)�A
     * �{SDK�������I�Ƀl�N�X�g�G���W���̃��O�C����ʂɃ��_�C���N�g���܂��i���[�U�[�ɂ͔F�؉�ʂ��\�������j�B
     * ���[�U�[���F�؂�����A�l�N�X�g�G���W���T�[�o�[����F�؏��Ƌ��ɃA�v���P�[�V�����T�[�o�[��
     * ���_�C���N�g���܂��B���̍ۂ̃A�v���P�[�V�����T�[�o�[�̃��_�C���N�g��uri�ł��B
     *
     * @param request ���N�G�X�g
     * @param response ���X�|���X
     * @param client_id �N���C�A���gID
     * @param client_secret �N���C�A���g�V�[�N���b�g
     * @param redirect_uri �l�N�X�g�G���W�����O�C�����K�v�ȏꍇ�A ��x���O�C����ʂɑJ�ڂ��܂��B���O�C�����
     * �A�v���P�[�V�����T�[�o�[�̃��_�C���N�g����w�肵�܂��B
     * @throws NeApiClientException SDK�̎d�l���@���Ԉ���Ă���
     */
    public NeApiClient(HttpServletRequest request, HttpServletResponse response, String client_id, String client_secret, String redirect_uri) throws NeApiClientException, Exception {
        this._is_web = true;

        this._request = request;
        if (!(this._request instanceof HttpServletRequest)) {
            throw new NeApiClientException("request�ɂ�HttpServletRequest���w�肵�ĉ������B");
        }

        this._response = response;
        if (!(this._response instanceof HttpServletResponse)) {
            throw new NeApiClientException("response�ɂ�HttpServletResponse���w�肵�ĉ������B");
        }

        this._client_id = client_id;
        if (this._client_id == null) {
            throw new NeApiClientException("client_id�̓A�v�������->API�^�u�̃N���C�A���gID���w�肵�ĉ������B");
        }

        this._client_secret = client_secret;
        if (this._client_secret == null) {
            throw new NeApiClientException("client_secret�̓A�v�������->API�^�u�̃N���C�A���g�V�[�N���b�g���w�肵�ĉ������B");
        }

        this._redirect_uri = redirect_uri;
        if (this._redirect_uri == null) {
            throw new NeApiClientException("redirect_uri�̓��[�U�[�����O�C��������ɁA���_�C���N�g�����A�v���P�[�V�����T�[�o�[��URI���w�肵�ĉ������B");
        }
    }

    /**
     * ��x�F�؂�����A�o�b�`���񓯊��ŁA�O��̐ڑ���񂩂�ēx�ڑ�����ꍇ�͖{���\�b�h���g���܂��B
     * 
     * access_token��refresh_token�̐����F
     * �w�肷��l�́A�Ō��apiExecute����neLogin�Ăяo�������getAccessToken(),getRefreshToken()�̖߂�l�ł��B
     * ���ӁF���̒l�̓��[�U�[��(uid��)�ɊǗ�����K�v������܂��B�ʂ̃��[�U�[�̒l���w�肵��SDK�����s�����
     * �����[�U�[�̏��ɃA�N�Z�X���Ă��܂����߁A���d�ɂ����ӂ����肢���܂��B
     *
     * @param access_token NE API�ɂ���Ĕ��s���ꂽ�A�N�Z�X�g�[�N��
     * @param refresh_token NE API�ɂ���Ĕ��s���ꂽ���t���b�V���g�[�N��
     * @throws NeApiClientException SDK�̎d�l���@���Ԉ���Ă���
     */
    public NeApiClient(String access_token, String refresh_token) throws NeApiClientException, Exception {
        this._is_web = false;

        this._access_token = access_token;
        if (this._access_token == null) {
            throw new NeApiClientException("access_token�͍Ō��apiExecute����neLogin�Ăяo�������getAccessToken(),getRefreshToken()�̖߂�l��ݒ肵�ĉ������B");
        }

        this._refresh_token = refresh_token;
    }

    /**
     * �l�N�X�g�G���W�����O�C���̂ݎ��s���܂��B ���Ƀ��O�C�����Ă���ꍇ�A���O�C����̊�{����ԋp���܂��B
     * �܂����O�C�����Ă��Ȃ��ꍇ�A�l�N�X�g�G���W�����O�C����ʂɃ��_�C���N�g����A
     * ���������O�C�������ꍇ�Aredirect_uri�Ƀ��_�C���N�g����܂��B
     * ���_�C���N�g��ŁA�ēxneLogin���ĂԎ��ŁA���O�C���������[�U�[�̊�{����ԋp���܂��B
     * �iredirect_uri���C���X�^���X�����ォ��ύX�������ꍇ�p�j�B
     *
     * @param redirect_uri ���[�U�[���l�N�X�g�G���W�����O�C����������̃A�v���P�[�V�����T�[�o�[��URI
     * @return ���O�C���������[�U�[�̊�{���
     * @throws IOException ���o�͊֌W
     * @throws NeApiClientException SDK�̎d�l���@���Ԉ���Ă���
     */
    public HashMap<String, Object> neLogin(String redirect_uri) throws IOException, NeApiClientException, Exception {
        this._redirect_uri = redirect_uri;
        return (this.neLogin());
    }

    /**
     * �l�N�X�g�G���W�����O�C���̂ݎ��s���܂��B ���Ƀ��O�C�����Ă���ꍇ�A���O�C����̊�{����ԋp���܂��B
     * �܂����O�C�����Ă��Ȃ��ꍇ�A�l�N�X�g�G���W�����O�C����ʂɃ��_�C���N�g����A
     * ���������O�C�������ꍇ�Aredirect_uri�Ƀ��_�C���N�g����܂��B
     * ���_�C���N�g��ŁA�ēxneLogin���ĂԎ��ŁA���O�C���������[�U�[�̊�{����ԋp���܂��B
     * �iredirect_uri���C���X�^���X�����ォ��ύX���Ȃ��ꍇ�p�j�B
     *
     * @return ���O�C���������[�U�[�̊�{���
     * @throws IOException ���o�͊֌W
     * @throws NeApiClientException SDK�̎d�l���@���Ԉ���Ă���
     */
    public HashMap<String, Object> neLogin() throws IOException, NeApiClientException, Exception {
        HashMap<String, Object> response;

        if (!this._is_web) {
            throw new NeApiClientException("�{���\�b�h�́A��������HttpServletRequest�̃R���X���N�^�ŏ���������K�v������܂��B");
        }

        // �����o�ϐ���uid�y��state��ݒ�
        setUidAndState();
        if (this._response.isCommitted()) {
            return (null);
        }

        // API�T�[�o�[�̔F�؂����{
        response = this.AuthApi();

        // ���_�C���N�g���K�v�Ȃ烊�_�C���N�g����
        this.responseCheck(response);

        return (response);
    }

    /**
     * �l�N�X�g�G���W��API�����s���A���ʂ�Ԃ��܂��B
     * �ʏ��Web�A�v���P�[�V��������̎��s(��������HttpServletRequest�̃R���X�g���N�^)�̏ꍇ �l�N�X�g�G���W���ɔF�؂��܂��B
     * ���ɔF�؍ς݂Ńo�b�`���񓯊������Ŏ��{����ꍇ�́Aaccess_token��API�����{���܂��B
     * �iredirect_uri���C���X�^���X�����ォ��ύX�������ꍇ�E�p�����[�^������API�̏ꍇ�p�j�B
     *
     * @param path ���s����API��URL�̃z�X�g���ȍ~�̃p�X(Ex:/api_v1_master_stock/search)�B
     * @param api_params ���s����API�̓��̓p�����[�^�B�s�v�ȏꍇ�͏ȗ����ĉ������B
     * @param redirect_uri ���[�U�[���l�N�X�g�G���W�����O�C����������̃A�v���P�[�V�����T�[�o�[��URI
     * @return API�̎��s���ʁi�o�̓p�����[�^�j
     * @throws IOException ���o�͊֌W
     * @throws NeApiClientException SDK�̎d�l���@���Ԉ���Ă���ipath�j��
     */
    public HashMap<String, Object> apiExecute(String path, HashMap<String, String> api_params, String redirect_uri) throws IOException, NeApiClientException, Exception {
        this._redirect_uri = redirect_uri;
        return (this.apiExecute(path, api_params));
    }

    /**
     * �l�N�X�g�G���W��API�����s���A���ʂ�Ԃ��܂��B
     * �ʏ��Web�A�v���P�[�V��������̎��s(��������HttpServletRequest�̃R���X�g���N�^)�̏ꍇ �l�N�X�g�G���W���ɔF�؂��܂��B
     * ���ɔF�؍ς݂Ńo�b�`���񓯊������Ŏ��{����ꍇ�́Aaccess_token��API�����{���܂��B
     * �iredirect_uri���C���X�^���X�����ォ��ύX���Ȃ��ꍇ�E�p�����[�^������API�̏ꍇ�p�j�B
     *
     * @param path ���s����API��URL�̃z�X�g���ȍ~�̃p�X(Ex:/api_v1_master_stock/search)�B
     * @param api_params ���s����API�̓��̓p�����[�^�B�s�v�ȏꍇ�͏ȗ����ĉ������B
     * @return API�̎��s���ʁi�o�̓p�����[�^�j
     * @throws IOException ���o�͊֌W
     * @throws NeApiClientException SDK�̎d�l���@���Ԉ���Ă���ipath�j��
     */
    public HashMap<String, Object> apiExecute(String path, HashMap<String, String> api_params) throws IOException, NeApiClientException, Exception {
        HashMap<String, Object> response;

        if (this._is_web) {
            // �����o�ϐ���uid�y��state��ݒ�
            setUidAndState();
            if (this._response.isCommitted()) {
                return (null);
            }

            // �܂��A�N�Z�X�g�[�N�����擾���Ă��Ȃ��ꍇ�̂�
            if( this._access_token == null ) {
                // API�T�[�o�[�̔F�؂����{
                response = this.AuthApi();
                if (!responseCheck(response)) {
                    return (response);
                }
            }
        }

        HashMap<String, String> params = (HashMap<String, String>) api_params.clone();
        params.put(KEY_ACCESS_TOKEN, this._access_token);
        if (this._refresh_token != null) {
            params.put(KEY_REFRESH_TOKEN, this._refresh_token);
        }
        response = this.post(SERVER_HOST_API + path, params);

        // ���_�C���N�g���K�v�Ȃ烊�_�C���N�g����
        this.responseCheck(response);

        return (response);
    }

    /**
     * �l�N�X�g�G���W��API�����s���A���ʂ�Ԃ��܂��B
     * �ʏ��Web�A�v���P�[�V��������̎��s(��������HttpServletRequest�̃R���X�g���N�^)�̏ꍇ �l�N�X�g�G���W���ɔF�؂��܂��B
     * ���ɔF�؍ς݂Ńo�b�`���񓯊������Ŏ��{����ꍇ�́Aaccess_token��API�����{���܂��B
     * �iredirect_uri���C���X�^���X�����ォ��ύX�������ꍇ�E�p�����[�^���Ȃ�API�̏ꍇ�p�j�B
     *
     * @param path ���s����API��URL�̃z�X�g���ȍ~�̃p�X(Ex:/api_v1_master_stock/search)�B
     * @param redirect_uri ���[�U�[���l�N�X�g�G���W�����O�C����������̃A�v���P�[�V�����T�[�o�[��URI
     * @return API�̎��s���ʁi�o�̓p�����[�^�j
     * @throws IOException ���o�͊֌W
     * @throws NeApiClientException SDK�̎d�l���@���Ԉ���Ă���ipath�j��
     */
    public HashMap<String, Object> apiExecute(String path, String redirect_uri) throws IOException, NeApiClientException, Exception {
        this._redirect_uri = redirect_uri;
        return (apiExecute(path));
    }

    /**
     * �l�N�X�g�G���W��API�����s���A���ʂ�Ԃ��܂��B
     * �ʏ��Web�A�v���P�[�V��������̎��s(��������HttpServletRequest�̃R���X�g���N�^)�̏ꍇ �l�N�X�g�G���W���ɔF�؂��܂��B
     * ���ɔF�؍ς݂Ńo�b�`���񓯊������Ŏ��{����ꍇ�́Aaccess_token��API�����{���܂��B
     * �iredirect_uri���C���X�^���X�����ォ��ύX���Ȃ��ꍇ�E�p�����[�^���Ȃ�API�̏ꍇ�p�j�B
     *
     * @param path ���s����API��URL�̃z�X�g���ȍ~�̃p�X(Ex:/api_v1_master_stock/search)�B
     * @return API�̎��s���ʁi�o�̓p�����[�^�j
     * @throws IOException ���o�͊֌W
     * @throws NeApiClientException SDK�̎d�l���@���Ԉ���Ă���ipath�j��
     */
    public HashMap<String, Object> apiExecute(String path) throws IOException, NeApiClientException, Exception {
        HashMap<String, String> api_params = new HashMap();
        return (apiExecute(path, api_params));
    }


    /**
     * �l�N�X�g�G���W�����O�C�����s�v�ȃl�N�X�g�G���W��API�����s���A���ʂ�Ԃ��܂��B
     *
     * @param path ���s����API��URL�̃z�X�g���ȍ~�̃p�X(Ex:/api_app/company)�B
     * @return API�̎��s���ʁi�o�̓p�����[�^�j
     * @throws IOException ���o�͊֌W
     * @throws NeApiClientException SDK�̎d�l���@���Ԉ���Ă���ipath�j��
     */
    public HashMap<String, Object> apiExecuteNoRequiredLogin(String path) throws IOException, NeApiClientException, Exception {
        HashMap<String, String> api_params = new HashMap();
        return (this.apiExecuteNoRequiredLogin(path, api_params));
    }

    /**
     * �l�N�X�g�G���W�����O�C�����s�v�ȃl�N�X�g�G���W��API�����s���A���ʂ�Ԃ��܂��B
     *
     * @param path ���s����API��URL�̃z�X�g���ȍ~�̃p�X(Ex:/api_app/company)�B
     * @param api_params ���s����API�̓��̓p�����[�^�B�s�v�ȏꍇ�͏ȗ����ĉ������B
     * @return API�̎��s���ʁi�o�̓p�����[�^�j
     * @throws IOException ���o�͊֌W
     * @throws NeApiClientException SDK�̎d�l���@���Ԉ���Ă���ipath�j��
     */
    public HashMap<String, Object> apiExecuteNoRequiredLogin(String path, HashMap<String, String> api_params) throws IOException, NeApiClientException, Exception {
    	api_params.put(KEY_CLIENT_ID, this._client_id);
    	api_params.put(KEY_CLIENT_SECRET, this._client_secret);

        HashMap<String, Object> response = this.post(SERVER_HOST_API + path, api_params);
        return (response);
}

    protected void setUidAndState() throws IOException, NeApiClientException {
        this._uid = this._request.getParameter(KEY_UID);
        this._state = this._request.getParameter(KEY_STATE);

        // uid����state���Ȃ��Ȃ�NE�Ƀ��O�C��
        if (this._uid == null || this._state == null) {
            redirectNeLogin();
        }
    }

    protected HashMap<String, Object> AuthApi() throws IOException, NeApiClientException {
        HashMap<String, String> params = new HashMap();
        params.put(KEY_UID, this._uid);
        params.put(KEY_STATE, this._state);

        HashMap<String, Object> response = this.post(SERVER_HOST_API + PATH_OAUTH, params);
        return (response);
    }

    protected void redirectNeLogin() throws IOException {
        HashMap<String, String> params = new HashMap();
        params.put(KEY_CLIENT_ID, this._client_id);
        params.put(KEY_CLIENT_SECRET, this._client_secret);
        if (this._redirect_uri != null) {
            params.put(KEY_REDIRECT_URI, this._redirect_uri);
        }

        String url = SERVER_HOST_NE + PATH_LOGIN + "?" + getUrlParams(params);
        this._response.sendRedirect(url);
    }

    protected boolean responseCheck(HashMap<String, Object> response) throws IOException, NeApiClientException {
        String result = (String) response.get(KEY_RESULT);
        String access_token =  (String) response.get(KEY_ACCESS_TOKEN) ;
        String refresh_token =  (String) response.get(KEY_REFRESH_TOKEN) ;

        if (result == null) throw new NeApiClientException("�N���C�A���gID�E�V�[�N���b�g��w�肵���p�X�����������m�F���ĉ������B");
        if ( access_token != null ) this._access_token = access_token ;
        if ( refresh_token != null ) this._refresh_token = refresh_token ;

        // ����I��
        if (result.equals(RESULT_SUCCESS) == true) return (true);

        // ���_�C���N�g�̏ꍇ
        if (result.equals(RESULT_REDIRECT) == true) {
            // Web�A�v���P�[�V��������̎��s�̏ꍇ�A���_�C���N�g�����{
            if (this._is_web) {
                this.redirectNeLogin();
            }
            return (false);
        }
        // �G���[�̏ꍇ
        return (false);
    }

    protected HashMap<String, Object> post(String url_str, HashMap params) throws MalformedURLException, IOException {
        // �A�h���X�ݒ�A�w�b�_�[���ݒ�
        URL url = new URL(url_str);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        con.setUseCaches(false);
        if (this._redirect_uri != null) {
            con.setRequestProperty("Referer", "https://" + new URL(this._redirect_uri).getHost());
        }
        PrintWriter pw = new PrintWriter(con.getOutputStream());
        pw.print(getUrlParams(params));
        pw.close() ;

        InputStream is = con.getInputStream();

        HashMap<String, Object> result = new ObjectMapper().readValue(is, HashMap.class);

        return (result);
    }

    protected static String getUrlParams(HashMap<String, String> params) {
        return urlEncodeUTF8(params);
    }

    protected static String urlEncodeUTF8(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    }

    protected static String urlEncodeUTF8(HashMap<?, ?> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(String.format("%s=%s",
                    urlEncodeUTF8(entry.getKey().toString()),
                    urlEncodeUTF8(entry.getValue().toString())
            ));
        }
        return sb.toString();
    }

}

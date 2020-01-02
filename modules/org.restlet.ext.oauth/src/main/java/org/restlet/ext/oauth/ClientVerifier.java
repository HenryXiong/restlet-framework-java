/**
 * Copyright 2005-2019 Talend
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of Talend S.A.
 */

package org.restlet.ext.oauth;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.engine.util.StringUtils;
import org.restlet.ext.oauth.internal.Client;
import org.restlet.ext.oauth.internal.ClientManager;
import org.restlet.security.SecretVerifier;

/**
 * Verifier for OAuth 2.0 Token Endpoints.<br>
 * Verify incoming requests with client credentials. Typically, use it with ChallengeAuthenticator.
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 * @deprecated Will be removed in next minor release.
 */
@Deprecated
public class ClientVerifier extends SecretVerifier {
    /**
     * Indicates whether the credentials may be found in the Web form payload in case the request has no
     * challengeResponse (cf {@link Request#getChallengeResponse()}). If so, it looks for the
     * {@link OAuthServerResource#CLIENT_ID} and {@link OAuthServerResource#CLIENT_SECRET} parameters.
     */
    private boolean acceptBodyMethod = false;

    private Context context;

    public ClientVerifier(Context context) {
        this.context = context;
    }

    /**
     * Indicates if the verifier can find credentials in the Web Form entity of the request in case the request has no
     * challengeResponse (cf {@link Request#getChallengeResponse()}). If so, it looks for the
     * {@link OAuthServerResource#CLIENT_ID} and {@link OAuthServerResource#CLIENT_SECRET} parameters.
     * 
     * @return True if the verifier can find credentials in the Web Form entity of the request.
     */
    public boolean isAcceptBodyMethod() {
        return acceptBodyMethod;
    }

    /**
     * Indicates whether the verifier can find credentials in the Web Form entity of the request in case the request has
     * no challengeResponse (cf {@link Request#getChallengeResponse()}). If so, it looks for the
     * {@link OAuthServerResource#CLIENT_ID} and {@link OAuthServerResource#CLIENT_SECRET} parameters.
     * 
     * @param acceptBodyMethod
     *            True to looks for credentials inside the request's Web Form, false otherwise.
     */
    public void setAcceptBodyMethod(boolean acceptBodyMethod) {
        this.acceptBodyMethod = acceptBodyMethod;
    }

    /**
     * In case the credentials are invalid, the response is completed with a representation generated by using
     * {@link AuthorizationServerResource#responseErrorRepresentation(OAuthException)}.
     */
    @Override
    public int verify(Request request, Response response) {
        final String clientId;
        final char[] clientSecret;

        ChallengeResponse cr = request.getChallengeResponse();

        if (cr == null) {
            if (!isAcceptBodyMethod()) {
                return RESULT_MISSING;
            }

            // Alternative method...
            Form params = new Form(request.getEntity());

            clientId = params.getFirstValue(OAuthServerResource.CLIENT_ID);
            if (StringUtils.isNullOrEmpty(clientId)) {
                return RESULT_MISSING;
            }

            String secret = params.getFirstValue(OAuthServerResource.CLIENT_SECRET);
            if (StringUtils.isNullOrEmpty(secret)) {
                clientSecret = new char[0];
            } else {
                clientSecret = secret.toCharArray();
            }
            // Restore the body
            request.setEntity(params.getWebRepresentation());
        } else if (!ChallengeScheme.HTTP_BASIC.equals(cr.getScheme())) {
            // XXX: May be unsupported
            return RESULT_UNSUPPORTED;
        } else {
            clientId = cr.getIdentifier();
            clientSecret = cr.getSecret();
        }

        int result = verify(clientId, clientSecret);
        if (result == RESULT_VALID) {
            request.getClientInfo().setUser(createUser(clientId, request, response));
        } else {
            OAuthException exception = new OAuthException(OAuthError.invalid_client, "Invalid client credentials.",
                    null);
            response.setEntity(OAuthServerResource.responseErrorRepresentation(exception));
        }
        return result;
    }

    @Override
    public int verify(String identifier, char[] secret) {
        ClientManager clients = (ClientManager) context.getAttributes().get(ClientManager.class.getName());

        Client client = clients.findById(identifier);

        if (client == null) {
            return RESULT_UNKNOWN;
        }

        char[] s = client.getClientSecret();
        if (!SecretVerifier.compare(s, secret)) {
            return RESULT_INVALID;
        }

        return RESULT_VALID;
    }
}

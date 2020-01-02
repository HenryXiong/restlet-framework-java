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

package org.restlet.test.bench;

import org.restlet.data.MediaType;
import org.restlet.representation.FileRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

public class TestPostClient {

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        ClientResource resource = new ClientResource("http://localhost:8554/");

        FileRepresentation fr = new FileRepresentation("file:///c:/test.mpg",
                MediaType.VIDEO_MPEG);
        System.out.println("Size sent: " + fr.getSize());

        try {
            resource.post(fr);
        } catch (ResourceException e) {
            // Nothing
        }

        System.out.println("Status: " + resource.getStatus());
        long endTime = System.currentTimeMillis();
        System.out.println("Duration: " + (endTime - startTime) + " ms");
    }

}

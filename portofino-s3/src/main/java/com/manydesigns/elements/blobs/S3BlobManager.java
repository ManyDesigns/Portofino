/*
 * Copyright (C) 2005-2021 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.manydesigns.elements.blobs;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.google.common.escape.UnicodeEscaper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Properties;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class S3BlobManager implements BlobManager{
    public static final String S3_PROPERTIES_PREFIX = "x-amz-meta-";
    //**************************************************************************
    // Fields
    //**************************************************************************


    final private String bucketName;
    final AmazonS3 s3;
    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(SimpleBlobManager.class);

    //**************************************************************************
    // Constructors and initialization
    //**************************************************************************


    public S3BlobManager(String region, String bucketName) {
        this.bucketName = bucketName;

        AWSCredentialsProvider credential = new DefaultAWSCredentialsProviderChain();
        this.s3 = AmazonS3ClientBuilder.standard().withCredentials( credential ).withRegion( Regions.fromName(region)).build();
    }

    public S3BlobManager(String region, String bucketName, String endPoint) {
        this.bucketName = bucketName;
        AwsClientBuilder.EndpointConfiguration  endpointConfiguration = new AwsClientBuilder.EndpointConfiguration( endPoint, region );

        AWSCredentialsProvider credential = new DefaultAWSCredentialsProviderChain();
        this.s3 = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration( endpointConfiguration )
                .withPathStyleAccessEnabled(true)
                .build();
    }                                                                              

    //**************************************************************************
    // Methods
    //**************************************************************************



    public void ensureValidCode(String code) {
        if (!StringUtils.isAlphanumeric(code)) {
            throw new IllegalArgumentException(
                    "Code is not alphanumeric: " + code);
        }
    }

    @Override
    public void loadMetadata(Blob blob) throws IOException {
        ensureValidCode(blob.getCode());
        blob.setMetaProperties(loadMetaProperties(blob.getCode()));
    }

    public Properties loadMetaProperties(String code) throws IOException {
        Properties metaProperties = new Properties();
        try {
            ObjectMetadata metadata = s3.getObjectMetadata(this.bucketName, code );
            for(String key : metadata.getUserMetadata().keySet()){
                String cleanKey=key.replaceAll( S3_PROPERTIES_PREFIX, "" );
                if( "filename".equals(cleanKey)){
                    metaProperties.put(cleanKey, URLDecoder.decode( metadata.getUserMetadata().get( key ),"UTF-8"));
                }else
                    metaProperties.put(cleanKey,  metadata.getUserMetadata().get( key ) );
            }
            metaProperties.put(Blob.SIZE_PROPERTY, Long.toString( metadata.getContentLength()));

        } catch (AmazonServiceException e) {
            throw new IOException( e.getMessage() );
        }
        return metaProperties;
    }

    @Override
    public InputStream openStream(Blob blob) throws IOException {
        ensureValidCode(blob.getCode());
        blob.setInputStream(getDataFile(blob.getCode()));
        return blob.getInputStream();
    }

    public InputStream getDataFile(String code) throws IOException {
        try {
              S3Object s3Object = s3.getObject(this.bucketName, code );
              return s3Object.getObjectContent();
        } catch (AmazonServiceException e) {
            throw new IOException( e.getMessage() );
        }

    }

    @Override
    public void save(Blob blob) throws IOException {
        ensureValidCode(blob.getCode());
        try {
            InputStream encryptInputStream;
            InputStream inputStream = blob.getInputStream();
            ObjectMetadata metadata = new ObjectMetadata();
            Properties properties = blob.getMetaProperties();

            metadata.setContentEncoding("UTF-8");
            metadata.setContentType("plain/text");


            for(Object obj : properties.keySet()){
                metadata.addUserMetadata( "app_creator", "Portofino" );
                if("filename".equals(obj.toString())) {
                    //metadata.addUserMetadata( obj.toString(),properties.get( obj ).toString() );

                    metadata.addUserMetadata( obj.toString(), URLEncoder.encode(properties.get( obj ).toString(), "UTF-8") );
                }
                else
                    metadata.addUserMetadata( obj.toString(),properties.get( obj ).toString());

            }

            PutObjectResult result;
            if(blob.isEncrypted()){
                encryptInputStream = BlobUtils.encrypt(inputStream,blob.getEncryptionType());
                result = s3.putObject(bucketName, blob.getCode(), inputStream, metadata);
            } else {
                result = s3.putObject(bucketName, blob.getCode(), inputStream, metadata);
            }

        } catch (AmazonServiceException e) {
            throw new IOException( e.getMessage() );
        }
        blob.dispose();
    }

    @Override
    public boolean delete(Blob blob) {
        String code = blob.getCode();
        ensureValidCode(code);
        boolean success = true;
        try {
            DeleteObjectRequest request = new DeleteObjectRequest( bucketName, code );
            s3.deleteObject( request );
        } catch (Exception e) {
            logger.warn("Cound not delete meta file", e);
            success = false;
        }
        
        return success;
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************


    public String getBucketName() {
        return bucketName;
    }
}

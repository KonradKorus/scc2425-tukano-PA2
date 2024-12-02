package tukano.impl.rest;

import jakarta.inject.Singleton;
import tukano.api.Blobs;
import tukano.api.rest.RestBlobs;
import tukano.impl.JavaBlobs;
import tukano.impl.JavaUsers;


@Singleton
public class RestBlobsResource extends RestResource implements RestBlobs {

    final Blobs impl;

    public RestBlobsResource() {
        this.impl = JavaBlobs.getInstance();
    }

    @Override
    public void upload(String blobId, byte[] bytes, String token) {
        try {
            var session = JavaUsers.validateSession(null);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Unauthorized");
        }

        super.resultOrThrow(impl.upload(blobId, bytes, token));
    }

    @Override
    public byte[] download(String blobId, String token) {
        try {
            var session = JavaUsers.validateSession(null);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Unauthorized");
        }

        return super.resultOrThrow(impl.download(blobId, token));
    }

    @Override
    public void delete(String blobId, String token) {
        try {
            var session = JavaUsers.validateSession(JavaUsers.ADMIN);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Unauthorized");
        }

        super.resultOrThrow(impl.delete(blobId, token));
    }

    @Override
    public void deleteAllBlobs(String userId, String password) {
        super.resultOrThrow(impl.deleteAllBlobs(userId, password));
    }
}

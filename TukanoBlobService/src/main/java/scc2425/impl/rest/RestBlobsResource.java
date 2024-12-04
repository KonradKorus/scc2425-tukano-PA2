package scc2425.impl.rest;

import jakarta.inject.Singleton;
import scc2425.api.Blobs;
import scc2425.api.rest.RestBlobs;
import scc2425.impl.JavaBlobs;

@Singleton
public class RestBlobsResource extends RestResource implements RestBlobs {

    final Blobs impl;

    public RestBlobsResource() {
        this.impl = JavaBlobs.getInstance();
    }

    @Override
    public void upload(String blobId, byte[] bytes, String token) {

        super.resultOrThrow(impl.upload(blobId, bytes, token));
    }

    @Override
    public byte[] download(String blobId, String token) {
        return super.resultOrThrow(impl.download(blobId, token));
    }

    @Override
    public void delete(String blobId, String token) {
        super.resultOrThrow(impl.delete(blobId, token));
    }

    @Override
    public void deleteAllBlobs(String userId, String password) {
        super.resultOrThrow(impl.deleteAllBlobs(userId, password));
    }
}

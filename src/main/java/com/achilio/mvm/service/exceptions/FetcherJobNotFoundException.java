package com.achilio.mvm.service.exceptions;

public class FetcherJobNotFoundException extends NotFoundException {

  public FetcherJobNotFoundException(String fetcherJobId) {
    super(String.format("FetcherJob %s not found", fetcherJobId));
  }
}

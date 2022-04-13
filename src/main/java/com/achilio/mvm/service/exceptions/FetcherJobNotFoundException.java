package com.achilio.mvm.service.exceptions;

public class FetcherJobNotFoundException extends NotFoundException {

  public FetcherJobNotFoundException(String fetcherJobId) {
    super(String.format("Job %s not found", fetcherJobId));
  }
}

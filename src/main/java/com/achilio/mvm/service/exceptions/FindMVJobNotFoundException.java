package com.achilio.mvm.service.exceptions;

public class FindMVJobNotFoundException extends NotFoundException {
  public FindMVJobNotFoundException(String findMVJobId) {
    super(String.format("Find MV Job %s not found", findMVJobId));
  }
}

package com.achilio.mvm.service.services;

import com.achilio.mvm.service.events.Event;

public interface PublisherService {

  void handleEvent(Event event);

}

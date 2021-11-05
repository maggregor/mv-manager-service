package com.achilio.mvm.service.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class DateUtils {

  public static Date getPastDate(int days) {
    ZoneId defaultZoneId = ZoneId.systemDefault();
    LocalDate localDate = LocalDate.now().minusDays(days);
    return Date.from(localDate.atStartOfDay(defaultZoneId).toInstant());
  }
}

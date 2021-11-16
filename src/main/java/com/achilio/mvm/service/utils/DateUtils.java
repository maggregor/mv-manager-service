package com.achilio.mvm.service.utils;

import java.time.LocalDate;
import java.time.ZoneId;

public class DateUtils {

  public static LocalDate getPastDate(LocalDate from, int days) {
    ZoneId defaultZoneId = ZoneId.systemDefault();
    return from.minusDays(days);
  }

  public static LocalDate getPastDate(int days) {
    return getPastDate(LocalDate.now(), days);
  }
}

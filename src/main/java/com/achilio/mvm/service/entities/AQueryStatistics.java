package com.achilio.mvm.service.entities;

import java.util.Date;
import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public abstract class AQueryStatistics {

  private Date startAt = null;
  private Date endAt = null;
  private boolean cached = false;

}

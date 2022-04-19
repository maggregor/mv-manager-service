package com.achilio.mvm.service.workflows;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Component;

@Component
public class DefaultJpaTransactionManager extends JpaTransactionManager {

  private final DataSource dataSource;

  public DefaultJpaTransactionManager(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Bean
  @Primary
  public JpaTransactionManager jpaTransactionManager() {
    final JpaTransactionManager tm = new JpaTransactionManager();
    tm.setDataSource(dataSource);
    return tm;
  }
}

package com.alwaysmart.optimizer;

import java.util.Set;

public interface OptimizeRunner {

	Set<MViewField> optimize(OptimizeContext context);

}

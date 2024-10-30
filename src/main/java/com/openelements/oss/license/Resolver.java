package com.openelements.oss.license;

import com.openelements.oss.license.data.Dependency;
import com.openelements.oss.license.data.Identifier;
import java.util.Set;

public interface Resolver {

    Set<Dependency> resolve(Identifier identifier);
}

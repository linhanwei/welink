package com.welink.promotion.drools

import org.kie.api.KieServices
import org.kie.api.runtime.KieContainer
import org.kie.api.runtime.KieSession
import spock.lang.Shared
import spock.lang.Specification


/**
 * Created by saarixx on 8/3/15.
 */
class DroolsRuleTests extends Specification {

    KieServices ks = KieServices.Factory.get();
    KieContainer kContainer = ks.getKieClasspathContainer();
    KieSession kSession = kContainer.newKieSession("promotion-rules");

    def setup() {
        kSession = kContainer.newKieSession("promotion-rules");
    }

    def cleanup() {
        kSession.dispose()
    }


    def "maximum of two numbers"() {
        expect:
        Math.max(a, b) == c
        where:
        a << [3, 5, 9]
        b << [7, 4, 9]
        c << [7, 5, 9]
    }

    def "minimum of #a and #b is #c"() {
        expect:
        Math.min(a, b) == c
        where:
        a | b || c
        3 | 7 || 3
        5 | 4 || 4
        9 | 9 || 9
    }
}
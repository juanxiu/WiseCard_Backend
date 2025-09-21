package com.example.demo.grpc;

import com.sub.grpc.CardData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GRpcTest {
    
    @Test
    public void testGRpcClassAccess() {
        // gRPC 클래스에 접근할 수 있는지 테스트
        CardData.CrawledBenefitList.Builder builder = CardData.CrawledBenefitList.newBuilder();
        assertNotNull(builder);
        
        CardData.CrawledBenefit.Builder benefitBuilder = CardData.CrawledBenefit.newBuilder();
        assertNotNull(benefitBuilder);
        
        CardData.Benefit.Builder benefitDataBuilder = CardData.Benefit.newBuilder();
        assertNotNull(benefitDataBuilder);
    }
}

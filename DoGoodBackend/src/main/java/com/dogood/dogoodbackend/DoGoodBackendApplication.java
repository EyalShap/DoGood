package com.dogood.dogoodbackend;
import com.dogood.dogoodbackend.domain.externalAIAPI.ProxyKeywordExtractor;
import com.dogood.dogoodbackend.domain.posts.MemoryVolunteeringPostRepository;
import com.dogood.dogoodbackend.domain.posts.PostsFacade;
import com.dogood.dogoodbackend.domain.posts.VolunteeringPostDTO;
import com.dogood.dogoodbackend.domain.posts.VolunteeringPostRepository;
import com.dogood.dogoodbackend.domain.volunteerings.MemoryVolunteeringRepository;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringFacade;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;


@SpringBootApplication
public class DoGoodBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(DoGoodBackendApplication.class, args);
		VolunteeringPostRepository repo = new MemoryVolunteeringPostRepository();
		VolunteeringRepository repo1 = new MemoryVolunteeringRepository();

		VolunteeringFacade x = new VolunteeringFacade(repo1, null);
		PostsFacade facade = new PostsFacade(repo, x, null, new ProxyKeywordExtractor());
		facade.createVolunteeringPost("title", "description", null, 0);
		List<VolunteeringPostDTO> res = facade.searchByKeywords("title", null);
		int y = 0;
	}

}

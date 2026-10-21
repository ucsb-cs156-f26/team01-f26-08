package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Article;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

/** The ArticleRepository is a repository for Article entities. */
@Repository
@RepositoryRestResource(exported = false)
public interface ArticleRepository extends CrudRepository<Article, Long> {
  // /**
  //  * This method returns all Article entities with a given quarterYYYYQ.
  //  *
  //  * @param quarterYYYYQ quarter in the format YYYYQ (e.g. 20241 for Winter 2024, 20242 for
  // Spring
  //  *     2024, 20243 for Summer 2024, 20244 for Fall 2024)
  //  * @return all UCSBDate entities with a given quarterYYYYQ
  //  */
  // Iterable<UCSBDate> findAllByQuarterYYYYQ(String quarterYYYYQ);
}

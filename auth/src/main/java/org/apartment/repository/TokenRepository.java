package org.apartment.repository;

import java.util.List;
import java.util.Optional;
import org.apartment.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<Token, String> {
  @Query(value = """
      select t from Token t inner join User u\s
      on t.user.id = u.id\s
      where u.id = :id and (t.revoked = false)\s
      """)
  List<Token> findAllValidTokenByUser(@Param("id") String id);

  @Modifying
  @Query("DELETE FROM Token t WHERE t.user.id = :userId ")
  void deleteByUserId(@Param("userId") String userId);

  @Query("select t from Token t where t.token = :token")
  Optional<Token> findTokenByValue(@Param("token") String token);
}

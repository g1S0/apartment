package org.apartment.auth.repository;

import java.util.List;
import java.util.Optional;
import org.apartment.auth.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TokenRepository extends JpaRepository<Token, Long> {
  @Query(value = """
      select t from Token t inner join User u\s
      on t.user.id = u.id\s
      where u.id = :id and (t.revoked = false)\s
      """)
  List<Token> findAllValidTokenByUser(@Param("id") Long id);

  @Query("select t from Token t where t.token = :token")
  Optional<Token> findTokenByValue(@Param("token") String token);
}

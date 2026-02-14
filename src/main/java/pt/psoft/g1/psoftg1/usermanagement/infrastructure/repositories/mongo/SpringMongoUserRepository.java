package pt.psoft.g1.psoftg1.usermanagement.infrastructure.repositories.mongo;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.shared.services.Page;
import pt.psoft.g1.psoftg1.usermanagement.model.User;
import pt.psoft.g1.psoftg1.usermanagement.repositories.UserRepository;
import pt.psoft.g1.psoftg1.usermanagement.services.SearchUsersQuery;

import java.util.List;
import java.util.Optional;

@Repository
@Profile({"mongodb-redis", "mongotest"})
@CacheConfig(cacheNames = "users")
public interface SpringMongoUserRepository extends UserRepository, UserRepoCustom, CrudRepository<User, Long> {

    @Override
    @CacheEvict(allEntries = true)
    <S extends User> List<S> saveAll(Iterable<S> entities);

    @Override
    @Caching(evict = { @CacheEvict(key = "#p0.id", condition = "#p0.id != null"),
            @CacheEvict(key = "#p0.username", condition = "#p0.username != null") })
    <S extends User> S save(S entity);

    @Override
    @Cacheable
    Optional<User> findById(Long objectId);

    @Cacheable
    Optional<User> findByUsername(String username);

    @Cacheable
    List<User> findByNameName(String name);

    @Cacheable
    default User getById(final Long id) {
        final Optional<User> maybeUser = findById(id);
        return maybeUser.filter(User::isEnabled).orElseThrow(() -> new NotFoundException(User.class, id));
    }
}

interface UserRepoCustom {
    List<User> searchUsers(Page page, SearchUsersQuery query);
}

class UserRepoCustomImpl implements UserRepoCustom {
    private final MongoTemplate mongoTemplate;

    public UserRepoCustomImpl(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<User> searchUsers(final Page page, final SearchUsersQuery query) {
        Query q = new Query();
        Criteria or = new Criteria();
        boolean has = false;
        if (StringUtils.hasText(query.getUsername())) {
            or = or.orOperator(Criteria.where("username").is(query.getUsername()));
            has = true;
        }
        if (StringUtils.hasText(query.getFullName())) {
            or = has ? or.orOperator(Criteria.where("name.name").regex(".*" + query.getFullName() + ".*", "i"))
                    : Criteria.where("name.name").regex(".*" + query.getFullName() + ".*", "i");
            has = true;
        }
        if (has) {
            q.addCriteria(or);
        }
        q.skip((long) (page.getNumber() - 1) * page.getLimit());
        q.limit(page.getLimit());
        q.with(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        return mongoTemplate.find(q, User.class);
    }
}



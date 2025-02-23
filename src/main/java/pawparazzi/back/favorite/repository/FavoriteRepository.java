package pawparazzi.back.favorite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pawparazzi.back.favorite.entity.Favorite;
import pawparazzi.back.favorite.entity.primaryKey.FavoritePk;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, FavoritePk> {
}

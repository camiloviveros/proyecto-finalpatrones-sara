ppackage com.trafficanalysis.repository;

import com.trafficanalysis.model.Detection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetectionRepository extends JpaRepository<Detection, Long> {
    
    @Query("SELECT d FROM Detection d ORDER BY d.timestampMs")
    List<Detection> findAllOrderByTimestampAsc();
    
    @Query("SELECT d FROM Detection d ORDER BY d.timestampMs DESC")
    List<Detection> findAllOrderByTimestampDesc();
}
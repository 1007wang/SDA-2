package com.sda_2.Controller;

import com.sda_2.DTO.PopulationData;
import com.sda_2.DTO.QuarterPopulationData;
import com.sda_2.Service.PopulationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/population")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}, allowCredentials = "true")
public class PopulationController {

    private final PopulationService populationService;

    public PopulationController(PopulationService populationService) {
        this.populationService = populationService;
    }

    @GetMapping("/geographies")
    public ResponseEntity<List<String>> getAllGeographies() {
        return ResponseEntity.ok(populationService.getAllGeographies());
    }

    @GetMapping("/geography/{geography}")
    public ResponseEntity<PopulationData> getByGeography(@PathVariable String geography) {
        return ResponseEntity.ok(populationService.getPopulationByGeography(geography));
    }

    @GetMapping("/quarter/{quarter}")
    public ResponseEntity<List<QuarterPopulationData>> getByQuarter(@PathVariable String quarter) {
        return ResponseEntity.ok(populationService.getPopulationByQuarter(quarter));
    }

    @GetMapping("/total/quarter/{quarter}")
    public ResponseEntity<Long> getTotalByQuarter(@PathVariable String quarter) {
        return ResponseEntity.ok(populationService.getTotalPopulationByQuarter(quarter));
    }
}

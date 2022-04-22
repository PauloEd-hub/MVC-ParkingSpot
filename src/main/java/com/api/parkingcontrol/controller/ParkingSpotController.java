package com.api.parkingcontrol.controller;


import com.api.parkingcontrol.dtos.ParkingSpotDTO;
import com.api.parkingcontrol.models.ParkingSpotModel;
import com.api.parkingcontrol.services.ParkingSpotService;
import org.hibernate.type.descriptor.java.LocaleTypeDescriptor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/parking-spot")
public class ParkingSpotController {

    final ParkingSpotService parkingSpotService;

    public ParkingSpotController(ParkingSpotService parkingSpotService) {
        this.parkingSpotService = parkingSpotService;
    }

    @PostMapping
    public ResponseEntity<Object> saveParkingSpot(@RequestBody @Valid ParkingSpotDTO parkingSpotDTO) {
        if(parkingSpotService.existsByLicensePlateCar(parkingSpotDTO.getLicensePlateCar())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(("Conflict: License Plate Car is already in use"));
        }
        if(parkingSpotService.existsByParkingSpotNumber(parkingSpotDTO.getParkingSpotNumber())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(("Conflict: Parking Spot isd ready in use!"));
        }
        if(parkingSpotService.existsByApartmentAndBlock(parkingSpotDTO.getApartment(), parkingSpotDTO.getBlock())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict Parking Spot already registered");
        }
        var parkingSpotModel = new ParkingSpotModel();
        BeanUtils.copyProperties(parkingSpotDTO, parkingSpotModel);
        parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));

        return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotService.save(parkingSpotModel));


    }

    @GetMapping
    public ResponseEntity<List<ParkingSpotModel>> getAllParkingSpots() {
        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.findAll());
    }


    @GetMapping("/{id}")
    public ResponseEntity<Object> getOneParkingSpot(@PathVariable(value = "id") UUID id) {
        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);

        if (!parkingSpotModelOptional.isPresent()) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotModelOptional.get());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteParkingSpot(@PathVariable(value = "id") UUID id) {
        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);

        if(!parkingSpotModelOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found");
        }
        parkingSpotService.delete(parkingSpotModelOptional.get());
        return ResponseEntity.status(HttpStatus.OK).body("Parking Spot deleted successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateParkingSpot(@PathVariable(value = "id") UUID id,
                                                    @RequestBody @Valid ParkingSpotDTO parkingSpotDTO){

        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);

        if(parkingSpotModelOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found");
        }
        var parkingSpotModel = new ParkingSpotModel();
        parkingSpotModel.setParkingSpotNumber(parkingSpotDTO.getParkingSpotNumber());
        parkingSpotModel.setLicensePlateCar(parkingSpotDTO.getLicensePlateCar());
        parkingSpotModel.setModelCar(parkingSpotModel.getModelCar());
        parkingSpotModel.setBrandCar(parkingSpotDTO.getBrandCar());
        parkingSpotModel.setColorCar(parkingSpotDTO.getColorCar());
        parkingSpotModel.setResponsibleName(parkingSpotDTO.getResponsibleName());
        parkingSpotModel.setApartment(parkingSpotDTO.getApartment());
        parkingSpotModel.setBlock(parkingSpotDTO.getBlock());

        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.save(parkingSpotModel));
    }
}

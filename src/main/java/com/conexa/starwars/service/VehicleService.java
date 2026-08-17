package com.conexa.starwars.service;

import com.conexa.starwars.client.SwapiItem;
import com.conexa.starwars.client.SwapiResourceClient;
import com.conexa.starwars.client.dto.VehicleProperties;
import com.conexa.starwars.dto.VehicleResponse;
import org.springframework.stereotype.Service;

@Service
public class VehicleService extends AbstractResourceService<VehicleProperties, VehicleResponse> {

    public VehicleService(SwapiResourceClient<VehicleProperties> vehicleSwapiResourceClient) {
        super(vehicleSwapiResourceClient);
    }

    @Override
    protected VehicleResponse map(SwapiItem<VehicleProperties> item) {
        return VehicleResponse.from(item);
    }
}

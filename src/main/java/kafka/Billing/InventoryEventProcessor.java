package kafka.Billing;

import com.fasterxml.jackson.databind.ObjectMapper;
import kafka.Billing.dto.SelectedProductDTO;
import kafka.Billing.entity.Cliente;
import kafka.Billing.entity.InventoryEvent;
import kafka.Billing.entity.Producto;
import kafka.Billing.repository.ProductoRepository;
import kafka.Billing.service.ClienteService;
import kafka.Billing.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class InventoryEventProcessor {


    @Autowired
    private ProductoService productoService;
    @Autowired
    private ClienteService clienteService;

    public static InventoryEvent parseInventoryEvent(String jsonString) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonString, InventoryEvent.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Float calculateFinalAmount(ArrayList<SelectedProductDTO> products) {
        Float finalAmount = 0f;
        for (SelectedProductDTO product : products) {
            Producto productoFinal = productoService.getById((product.getProductId()));
            //System.out.println(productoFinal.getValor() * product.getQuantity());
            finalAmount += productoFinal.getValor() * product.getQuantity();
        }

        return finalAmount;
    }

    public boolean enoughMoneyToBuyProducts(InventoryEvent inventoryEvent){
        Float finalAmount = calculateFinalAmount(inventoryEvent.getProductList());
        Double clienteMoney = getClienteMoney(inventoryEvent.getOrderId());
        return clienteMoney >= finalAmount;
    }

    private Double getClienteMoney(String clienteId){
        Cliente cliente = clienteService.getClienteById(Integer.parseInt(clienteId));
        return cliente.getDinero_disponible();

    }

    public String makePurchase(InventoryEvent inventoryEvent){
        if (enoughMoneyToBuyProducts(inventoryEvent)){
            return "compra realizada";
        }
        return "No se pudo realizar la compra";
    }
}

package com.ebusiness.controller;

import com.ebusiness.common.ApiResponse;
import com.ebusiness.entity.Address;
import com.ebusiness.service.AddressService;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {
  private final AddressService addressService;

  public AddressController(AddressService addressService) {
    this.addressService = addressService;
  }

  @GetMapping("")
  public ApiResponse<List<Address>> list() {
    return ApiResponse.success(addressService.listAddresses());
  }

  @PostMapping("")
  public ApiResponse<Address> create(@RequestBody Address payload) {
    return ApiResponse.success(addressService.createAddress(payload));
  }

  @PutMapping("/{id}")
  public ApiResponse<Address> update(@PathVariable Long id, @RequestBody Address payload) {
    return ApiResponse.success(addressService.updateAddress(id, payload));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable Long id) {
    addressService.deleteAddress(id);
    return ApiResponse.success("Deleted", null);
  }

  @PostMapping("/{id}/default")
  public ApiResponse<Void> setDefault(@PathVariable Long id) {
    addressService.setDefaultAddress(id);
    return ApiResponse.success("Default set", null);
  }
}

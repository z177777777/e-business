package com.ebusiness.service;

import com.ebusiness.common.BusinessException;
import com.ebusiness.common.ErrorCode;
import com.ebusiness.common.CurrentUserUtil;
import com.ebusiness.entity.Address;
import com.ebusiness.repository.AddressRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddressService {
  private final AddressRepository addressRepository;

  public AddressService(AddressRepository addressRepository) {
    this.addressRepository = addressRepository;
  }

  public List<Address> listAddresses() {
    Long uid = CurrentUserUtil.getCurrentUserId();
    return addressRepository.findByUserIdOrderByIsDefaultDescIdAsc(uid);
  }

  @Transactional
  public Address createAddress(Address address) {
    Long uid = CurrentUserUtil.getCurrentUserId();
    address.setUserId(uid);
    List<Address> existing = addressRepository.findByUserIdOrderByIsDefaultDescIdAsc(uid);
    if (existing.isEmpty()) {
      address.setIsDefault(true);
    } else if (Boolean.TRUE.equals(address.getIsDefault())) {
      // clear other defaults
      for (Address a : existing) {
        if (Boolean.TRUE.equals(a.getIsDefault())) {
          a.setIsDefault(false);
        }
      }
      addressRepository.saveAll(existing);
    } else {
      address.setIsDefault(false);
    }
    return addressRepository.save(address);
  }

  @Transactional
  public Address updateAddress(Long id, Address payload) {
    Long uid = CurrentUserUtil.getCurrentUserId();
    Optional<Address> opt = addressRepository.findByIdAndUserId(id, uid);
    if (!opt.isPresent()) throw new BusinessException(ErrorCode.NOT_FOUND);
    Address exist = opt.get();
    exist.setName(payload.getName());
    exist.setPhone(payload.getPhone());
    exist.setRegionPath(payload.getRegionPath());
    exist.setRegionText(payload.getRegionText());
    exist.setDetail(payload.getDetail());
    exist.setFull(payload.getFull());
    boolean setDefault = Boolean.TRUE.equals(payload.getIsDefault());
    if (setDefault) {
      List<Address> others = addressRepository.findByUserIdOrderByIsDefaultDescIdAsc(uid);
      for (Address a : others) {
        if (!a.getId().equals(id) && Boolean.TRUE.equals(a.getIsDefault())) {
          a.setIsDefault(false);
        }
      }
      addressRepository.saveAll(others);
      exist.setIsDefault(true);
    } else if (payload.getIsDefault() != null) {
      exist.setIsDefault(payload.getIsDefault());
    }
    return addressRepository.save(exist);
  }

  @Transactional
  public void deleteAddress(Long id) {
    Long uid = CurrentUserUtil.getCurrentUserId();
    Optional<Address> opt = addressRepository.findByIdAndUserId(id, uid);
    if (!opt.isPresent()) throw new BusinessException(ErrorCode.NOT_FOUND);
    addressRepository.delete(opt.get());
  }

  @Transactional
  public void setDefaultAddress(Long id) {
    Long uid = CurrentUserUtil.getCurrentUserId();
    Optional<Address> opt = addressRepository.findByIdAndUserId(id, uid);
    if (!opt.isPresent()) throw new BusinessException(ErrorCode.NOT_FOUND);
    List<Address> others = addressRepository.findByUserIdOrderByIsDefaultDescIdAsc(uid);
    for (Address a : others) {
      a.setIsDefault(a.getId().equals(id));
    }
    addressRepository.saveAll(others);
  }
}

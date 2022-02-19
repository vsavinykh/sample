package com.lanehealth.payrollservice.clients

import com.lanehealth.payrollservice.finch.contract.ApiClient
import com.lanehealth.payrollservice.finch.contract.client.FinchApi
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service
@Scope("prototype")
class FinchApiClients : ApiClient(){
}

@Service
class FinchClient(finchApiClient: FinchApiClients) : FinchApi(finchApiClient)
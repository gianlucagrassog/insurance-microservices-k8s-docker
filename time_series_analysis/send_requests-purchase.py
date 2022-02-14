import requests, time, math, random
import numpy as np
import pylab as plt
import matplotlib.pyplot as plt
from numpy import random
from requests.structures import CaseInsensitiveDict
 
url = "http://insurance.app.loc/purchases"
 
headers = CaseInsensitiveDict()
headers["Content-Type"] = "application/json"
 
data = '{"description":"my purchase", "user": "00009f2e713c090009b79b71", "policy": "61e405b852faff0009d6a190", "optionals_list": ["61e405bf52faff0009d6a191"]}'
 
x=random.exponential(scale=5, size=400)
x.sort()
noise = np.random.normal(0, 0.4, x.shape)
x = x + noise
 
x=x[:320]
max = np.amax(x)
for val in x:
    resp = requests.post(url, headers=headers, data=data)
    print(resp.status_code)
    time.sleep((max+0.5)-val)
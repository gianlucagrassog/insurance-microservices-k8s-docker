import requests, time, math, random
import numpy as np
import pylab as plt
import matplotlib.pyplot as plt
from numpy import random
from requests.structures import CaseInsensitiveDict

url = "http://insurance.app.loc/policies/"

headers = CaseInsensitiveDict()
headers["Content-Type"] = "application/json"

data = '{"name": "Policy1", "type": "BonusMalus", "description": "I am a insurance policy"}'

x=random.exponential(scale=110, size=400)
x.sort()
noise = np.random.normal(0, 0.2, x.shape)
x = x + noise

x=x[:100]
max = np.amax(x)
for val in x:
  resp = requests.post(url, headers=headers, data=data)
  print(resp.status_code)
  time.sleep((max+0.3)-val)
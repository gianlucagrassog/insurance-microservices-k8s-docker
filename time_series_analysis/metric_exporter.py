from prometheus_api_client import PrometheusConnect
from prometheus_api_client.utils import parse_datetime
from prometheus_api_client.metric_range_df import MetricRangeDataFrame
from datetime import *
from statsmodels.tsa.holtwinters import ExponentialSmoothing
import pandas as pd
import csv


def get_metric_value_in_time_range(metric_name, start_time, end_time, chunk_size):
    prom = PrometheusConnect(url="http://insurance.app.loc:30000", disable_ssl=True)

    metric_data = prom.get_metric_range_data(
        metric_name=metric_name,
        start_time=start_time,
        end_time=end_time,
        chunk_size=chunk_size,
    )

    df = MetricRangeDataFrame(metric_data)
    return df

def fix_ts(ts):
    ts.index = pd.to_datetime(ts.index, unit="s", utc=True)
    ts = ts.drop(columns = '__name__')
    ts = ts.drop(columns = 'app')
    ts = ts.drop(columns = 'instance')
    ts = ts.drop(columns = 'job')
    ts = ts.drop(columns = 'kubernetes_namespace')
    ts = ts.drop(columns = 'kubernetes_pod_name')
    ts = ts.drop(columns = 'pod_template_hash')
    ts = ts.astype(int)
    return ts

if __name__ == '__main__':
    
    start_time = datetime.now() - timedelta(hours=1)
    end_time = parse_datetime("now")
    chunk_size = timedelta(hours=1)
    
    # user
    u_ts = get_metric_value_in_time_range("user_counter_total", start_time, end_time, chunk_size)
    u_ts = fix_ts(u_ts)
    f = open('user_metrics.csv','w+')
    writer = csv.writer(f)
    u_ts.to_csv('user_metrics.csv')
    
    # policy
    po_ts = get_metric_value_in_time_range("policy_counter_total", start_time, end_time, chunk_size)
    po_ts = fix_ts(po_ts)
    f = open('policy_metrics.csv','w+')
    writer = csv.writer(f)
    po_ts.to_csv('policy_metrics.csv')
    
    # purchase
    pu_ts = get_metric_value_in_time_range("purchase_counter_total", start_time, end_time, chunk_size)
    pu_ts = fix_ts(pu_ts)
    f = open('purchase_metrics.csv','w+')
    writer = csv.writer(f)
    pu_ts.to_csv('purchase_metrics.csv')
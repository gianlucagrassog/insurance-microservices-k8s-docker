from statsmodels.tsa.seasonal import seasonal_decompose
from sklearn.metrics import mean_squared_error
from statsmodels.tsa.arima.model import ARIMA
from statsmodels.graphics.tsaplots import plot_acf, plot_pacf
from statsmodels.tsa.stattools import adfuller
from statsmodels.tools.eval_measures import rmse
from pmdarima import auto_arima
import pandas as pd
import matplotlib.pyplot as plt
import warnings

if __name__ == '__main__':
    
    warnings.filterwarnings("ignore")
    
    # utilizziamo Pandas per importare il dataset    
    ts = pd.read_csv('policy_metrics.csv', header=0, parse_dates=[0], index_col=[0])
    ts = ts.astype(int)
    print(ts)
    
    # poiché i dati provengono dalla stessa sorgente, non è necessario effettuare un resample
    
    # visualizziamo la serie in un grafico con l'utilizzo di matplotlib
    plt.figure(figsize=(24,10), dpi=100)
    plt.ylabel('Values', fontsize=14)
    plt.xlabel('Time', fontsize=14)
    plt.title('Time series', fontsize=16)
    plt.plot(ts,"-",label = 'ts', scalex=True)
    plt.legend(title='Series', fontsize=12)
    plt.show()
    # analizzando il grafico della serie, possiamo osservare un trend crescente
    
    result = seasonal_decompose(ts, model='mul', period=7)
    result.plot()
    plt.show()
    trend = result.trend.dropna()
    plt.figure(figsize=(24,10), dpi=100)
    plt.ylabel('Values', fontsize=14)
    plt.xlabel('Time', fontsize=14)
    plt.title('Time series', fontsize=16)
    plt.plot(ts,"-",label = 'ts', scalex=True)
    plt.plot(trend,"-",label = 'trend', scalex=True)
    plt.legend(title='Series', fontsize=12)
    plt.show()
    
    # la stagionalità si riferisce alle fluttuazioni periodiche nelle osservazioni
    # si può anche derivare dal grafico di autocorrelazione, se ha una forma sinusoidale
    print(plot_acf(trend, title='acf', lags=40)) # funzione di autocorrelazione
    plt.show()
    # nel nostro caso, la serie non presenta stagionalità
    
    # verifichiamo che la serie sia stazionaria:
    # se è stazionaria, possiamo usare la sua storia passata per prevedere il suo comportamento futuro
    # se non lo è, dobbiamo procedere con una trasformazione che la renda stazionaria
    # applichiamo il Dickey-Fuller test
    X = ts.values
    adft = adfuller(X)
    print('ADF Statistic: %f' % adft[0])
    print('p-value: %f' % adft[1])
    print('Critical Values:')
    for key, value in adft[4].items():
        print('\t%s: %.3f' % (key, value))
    # il valore ADF Statistic non è minore rispetto ai valori critici a differenti percentuali 
    # => non possiamo rifiutare le nostre null hypothesis => la serie non è stazionaria
    # per rendere stazionaria la serie, applichiamo un'operazione di Differenza, che effettua la differenza tra un'osservazione e la precedente
    ts_diff = ts.diff()
    plt.plot(ts_diff)
    plt.show()
    # applico nuovamente il test di verifica di stazionarietà
    ts_diff=ts_diff.dropna(axis=0)
    X2 = ts_diff.values
    adft2 = adfuller(X2)
    print('ADF Statistic: %f' % adft2[0])
    print('p-value: %f' % adft2[1])
    print('Critical Values:')
    for key, value in adft2[4].items():
        print('\t%s: %.3f' % (key, value))
    # anche questa volta, la serie risulta essere non stazionaria => ripeto l'operazione
    ts_diff2 = ts_diff.diff()
    plt.plot(ts_diff2)
    plt.show()
    # applico nuovamente il test di verifica di stazionarietà
    ts_diff2=ts_diff2.dropna(axis=0)
    X3 = ts_diff2.values
    adft3 = adfuller(X3)
    print('ADF Statistic: %f' % adft3[0])
    print('p-value: %f' % adft3[1])
    print('Critical Values:')
    for key, value in adft3[4].items():
        print('\t%s: %.3f' % (key, value))
    # serie stazionaria => Ok
    
    # il modello più appropriato per la nostra serie è ARIMA, in quanto nella nostra serie non è presente stagionalità (altrimenti avremmo potuto usare SARIMA)
    # dobbiamo ricavare i valori delle variabili p, d, q
    # p -> indica l'ordine autoregressivo
    # d -> indica l'ordine d'integrazione
    # q -> indica l'ordine della media mobile
    # alternativa: print(auto_arima(trend)) ---> ARIMA(3,2,3)
    # p è ricavabile dal grafico di autocorrelazione
    print(plot_acf(ts_diff2, title='acf', lags=40))
    plt.show()
    # p potrebbe essere 12
    # il valore q è possibile ricavarlo dal grafico di autocorrelazione parziale
    print(plot_pacf(ts_diff2, title='p_acf', lags=40))
    plt.show()
    # q = 0
    # d rappresenta il numero di procedimenti effettuati per rendere la serie stazionaria
    # nel nostro caso d = 2
    train = trend.iloc[:270] # 90%
    test = trend.iloc[270:] # 10%
    model = ARIMA(train, order=(12,2,0)) #training --> ARIMA(p,d,q)
    results = model.fit()
    start = len(train)
    end = len(train)+len(test)-1
    predictions = results.predict(start=start, end=end, dynamic=False, typ='levels')
    plt.figure(figsize=(24,10))
    plt.ylabel('Values', fontsize=14)
    plt.xlabel('Time', fontsize=14)
    plt.title('Time series', fontsize=16)
    plt.plot(train, "-", label='train')
    plt.plot(test, "-", label='real')
    plt.plot(predictions, "*", label='pred')
    plt.legend(title='Series')
    plt.show()
    # questo modello matcha abbastanza bene la nostra serie
    # aumentando di un'unità il valore di d, otteniamo un modello (ARIMA con order = (12,3,0)) che matcha quasi perfettamente la nostra serie

    train = trend.iloc[:270] # 90%
    test = trend.iloc[270:] # 10%
    model = ARIMA(train, order=(12,3,0)) #training
    results = model.fit()
    print(results.summary())
    
    # validazione del modello:
    # --- previsione sul test
    # sovrapponendo il test sulla previsione effettuata, possiamo valutare l'efficacia del modello
    start = len(train)
    end = len(train)+len(test)-1
    predictions = results.predict(start=start, end=end, dynamic=False, typ='levels')
    plt.figure(figsize=(24,10))
    plt.ylabel('Values', fontsize=14)
    plt.xlabel('Time', fontsize=14)
    plt.title('Time series', fontsize=16)
    plt.plot(train, "-", label='train')
    plt.plot(test, "-", label='real')
    plt.plot(predictions, "*", label='pred')
    plt.legend(title='Series')
    plt.show()
    # --- calcolo dell'errore quadratico medio
    # verifico che l'errore che otteniamo sia inferiore rispeto alla std
    # std
    print(trend.describe())
    print(test.describe())
    # più il valore che otteniamo è vicino a 0, più il modello si adatta ai dati
    print(rmse(test, predictions))
    print(mean_squared_error(test, predictions))
    # errors << std  Ok
    
    # possiamo ritenere il modello valido; possiamo procedere con il suo utilizzo per predire i dati futuri
    model = ARIMA(trend, order=(12,3,0))
    results = model.fit()
    fcast = results.predict(len(trend), len(trend)+72, typ='levels') # predizione di  campioni = 6 minuti
    
    plt.ylabel('Values', fontsize=14)
    plt.xlabel('Time', fontsize=14)
    plt.title('Time series', fontsize=16)
    plt.plot(ts, "-", label='ts')
    plt.plot(trend, "-", label='trend')
    plt.plot(fcast, "--", label='forecast')
    plt.legend(title='Series')
    plt.show()

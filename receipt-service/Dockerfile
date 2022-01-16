FROM python:3
WORKDIR /app
ENV FLASK_APP=receiptApp.py \
    PYTHONUNBUFFERED=1
COPY . ./
RUN pip install --no-cache-dir -r modules.txt
CMD ["python", "-m", "flask", "run", "--host", "0.0.0.0"]
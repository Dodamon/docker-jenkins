FROM python:3.9-buster
RUN apt-get update && apt-get install -y \
    gcc \
    libgl1-mesa-glx
RUN python -m pip install --upgrade pip
WORKDIR /app
COPY . /app
RUN pip install -r ./requirements.txt
EXPOSE 5000
CMD ["python", "-m", "flask", "run", "--host=0.0.0.0"]
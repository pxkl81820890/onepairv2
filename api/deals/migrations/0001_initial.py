# Generated by Django 3.0.3 on 2020-02-27 10:45

from django.db import migrations, models


class Migration(migrations.Migration):

    initial = True

    dependencies = [
    ]

    operations = [
        migrations.CreateModel(
            name='deals',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('name', models.CharField(max_length=155)),
                ('start', models.CharField(max_length=25)),
                ('end', models.CharField(max_length=25)),
                ('image', models.CharField(max_length=110)),
                ('vendors', models.CharField(max_length=41)),
                ('terms', models.CharField(max_length=4315)),
                ('category', models.CharField(max_length=126)),
            ],
        ),
    ]

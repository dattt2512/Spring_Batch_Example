package com.tdt.springbatch.config;

import com.tdt.springbatch.datasource.conf.DataSourceConfiguration;
import com.tdt.springbatch.entity.Order;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableBatchProcessing
@EnableAsync
public class BatchConfiguration {

    private final static Integer TOTAL_RECORDS = 1000000;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    DataSourceConfiguration masterDataSource;

    @Bean
    public AsyncItemProcessor<Order, Order> asyncProcessor() {

        AsyncItemProcessor<Order, Order> asyncItemProcessor = new AsyncItemProcessor<>();
        asyncItemProcessor.setDelegate(itemProcessor());
        asyncItemProcessor.setTaskExecutor(taskExecutor());

        return asyncItemProcessor;
    }

    @Bean
    public ItemProcessor<Order, Order> itemProcessor() {
        return (transaction) -> {
            Thread.sleep(1);
            return transaction;
        };
    }

    @Bean
    public Step asyncManagerStep(StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory
                .get("Asynchronous Processing : Read -> Process -> Write 1")
                .<Order, Order>chunk(10000)
                .reader(databaseReader())
//                .processor(asyncProcessor())
                .writer(itemFlatFileWriter())
                .build();
    }

    @Bean
    public ItemReader<Order> databaseReader() {

        JdbcPagingItemReader<Order> reader = new JdbcPagingItemReader<>();

        reader.setDataSource(this.masterDataSource.masterDataSource());
        reader.setFetchSize(1000);
        reader.setRowMapper(new OrderRowMapper());

        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("*");
        queryProvider.setFromClause("from orders");
        queryProvider.setWhereClause("where id <= " + TOTAL_RECORDS/8);

        Map<String, org.springframework.batch.item.database.Order> sortKeys = new HashMap<>(1);
        sortKeys.put("id", org.springframework.batch.item.database.Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);
        reader.setQueryProvider(queryProvider);

        return reader;
    }

//    @Bean
//    public AsyncItemWriter<Order> asyncWriter() {
//        AsyncItemWriter<Order> asyncItemWriter = new AsyncItemWriter<>();
//        asyncItemWriter.setDelegate(itemFlatFileWriter());
//        return asyncItemWriter;
//    }


    @Bean
    public FlatFileItemWriter<Order> itemFlatFileWriter() {
        return new FlatFileItemWriterBuilder<Order>()
                .name("Writer")
                .append(false)
                .resource(new FileSystemResource("orders1.csv"))
                .lineAggregator(new DelimitedLineAggregator<>() {
                    {
                        setDelimiter(";");
                        setFieldExtractor(new BeanWrapperFieldExtractor<>() {
                            {
                                setNames(new String[]{"id", "code", "stockCode", "price", "quantity", "total", "orderDate"});
                            }
                        });
                    }
                })
                .build();
    }

    @Bean
    public ItemReader<Order> databaseReader2() {

        JdbcPagingItemReader<Order> reader = new JdbcPagingItemReader<>();

        reader.setDataSource(this.masterDataSource.masterDataSource());
        reader.setFetchSize(1000);
        reader.setRowMapper(new OrderRowMapper());

        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("*");
        queryProvider.setFromClause("from orders");
        queryProvider.setWhereClause("where id <= " + TOTAL_RECORDS*2/8 + " and id > " + TOTAL_RECORDS/8);

        Map<String, org.springframework.batch.item.database.Order> sortKeys = new HashMap<>(1);
        sortKeys.put("id", org.springframework.batch.item.database.Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);
        reader.setQueryProvider(queryProvider);

        return reader;
    }

    @Bean
    public Step asyncManagerStep2(StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory
                .get("Asynchronous Processing : Read -> Process -> Write 2")
                .<Order, Order>chunk(10000)
                .reader(databaseReader2())
                .writer(itemFlatFileWriter2())
                .build();
    }


    @Bean
    public FlatFileItemWriter<Order> itemFlatFileWriter2() {
        return new FlatFileItemWriterBuilder<Order>()
                .name("Writer")
                .append(false)
                .resource(new FileSystemResource("orders2.csv"))
                .lineAggregator(new DelimitedLineAggregator<>() {
                    {
                        setDelimiter(";");
                        setFieldExtractor(new BeanWrapperFieldExtractor<>() {
                            {
                                setNames(new String[]{"id", "code", "stockCode", "price", "quantity", "total", "orderDate"});
                            }
                        });
                    }
                })
                .build();
    }

    @Bean
    public ItemReader<Order> databaseReader3() {

        JdbcPagingItemReader<Order> reader = new JdbcPagingItemReader<>();

        reader.setDataSource(this.masterDataSource.masterDataSource());
        reader.setFetchSize(1000);
        reader.setRowMapper(new OrderRowMapper());

        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("*");
        queryProvider.setFromClause("from orders");
        queryProvider.setWhereClause("where id <= " + TOTAL_RECORDS*3/8 + " and id > " + TOTAL_RECORDS*2/8); //====================

        Map<String, org.springframework.batch.item.database.Order> sortKeys = new HashMap<>(1);
        sortKeys.put("id", org.springframework.batch.item.database.Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);
        reader.setQueryProvider(queryProvider);

        return reader;
    }

    @Bean
    public Step asyncManagerStep3(StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory
                .get("Asynchronous Processing : Read -> Process -> Write 3")
                .<Order, Order>chunk(10000)
                .reader(databaseReader3())
                .writer(itemFlatFileWriter3())
                .build();
    }


    @Bean
    public FlatFileItemWriter<Order> itemFlatFileWriter3() {
        return new FlatFileItemWriterBuilder<Order>()
                .name("Writer")
                .append(false)
                .resource(new FileSystemResource("orders3.csv"))
                .lineAggregator(new DelimitedLineAggregator<>() {
                    {
                        setDelimiter(";");
                        setFieldExtractor(new BeanWrapperFieldExtractor<>() {
                            {
                                setNames(new String[]{"id", "code", "stockCode", "price", "quantity", "total", "orderDate"});
                            }
                        });
                    }
                })
                .build();
    }

    @Bean
    public ItemReader<Order> databaseReader4() {

        JdbcPagingItemReader<Order> reader = new JdbcPagingItemReader<>();

        reader.setDataSource(this.masterDataSource.masterDataSource());
        reader.setFetchSize(1000);
        reader.setRowMapper(new OrderRowMapper());

        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("*");
        queryProvider.setFromClause("from orders");
        queryProvider.setWhereClause("where id <= " + TOTAL_RECORDS*4/8 + " and id > " + TOTAL_RECORDS*3/8); //====================

        Map<String, org.springframework.batch.item.database.Order> sortKeys = new HashMap<>(1);
        sortKeys.put("id", org.springframework.batch.item.database.Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);
        reader.setQueryProvider(queryProvider);

        return reader;
    }

    @Bean
    public Step asyncManagerStep4(StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory
                .get("Asynchronous Processing : Read -> Process -> Write 4")
                .<Order, Order>chunk(10000)
                .reader(databaseReader4())
                .writer(itemFlatFileWriter4())
                .build();
    }


    @Bean
    public FlatFileItemWriter<Order> itemFlatFileWriter4() {
        return new FlatFileItemWriterBuilder<Order>()
                .name("Writer")
                .append(false)
                .resource(new FileSystemResource("orders4.csv"))
                .lineAggregator(new DelimitedLineAggregator<>() {
                    {
                        setDelimiter(";");
                        setFieldExtractor(new BeanWrapperFieldExtractor<>() {
                            {
                                setNames(new String[]{"id", "code", "stockCode", "price", "quantity", "total", "orderDate"});
                            }
                        });
                    }
                })
                .build();
    }

    @Bean
    public ItemReader<Order> databaseReader5() {

        JdbcPagingItemReader<Order> reader = new JdbcPagingItemReader<>();

        reader.setDataSource(this.masterDataSource.masterDataSource());
        reader.setFetchSize(1000);
        reader.setRowMapper(new OrderRowMapper());

        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("*");
        queryProvider.setFromClause("from orders");
        queryProvider.setWhereClause("where id <= " + TOTAL_RECORDS*5/8 + " and id > " + TOTAL_RECORDS*4/8); //====================

        Map<String, org.springframework.batch.item.database.Order> sortKeys = new HashMap<>(1);
        sortKeys.put("id", org.springframework.batch.item.database.Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);
        reader.setQueryProvider(queryProvider);

        return reader;
    }

    @Bean
    public Step asyncManagerStep5(StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory
                .get("Asynchronous Processing : Read -> Process -> Write 5")
                .<Order, Order>chunk(10000)
                .reader(databaseReader5())
                .writer(itemFlatFileWriter5())
                .build();
    }


    @Bean
    public FlatFileItemWriter<Order> itemFlatFileWriter5() {
        return new FlatFileItemWriterBuilder<Order>()
                .name("Writer")
                .append(false)
                .resource(new FileSystemResource("orders5.csv"))
                .lineAggregator(new DelimitedLineAggregator<>() {
                    {
                        setDelimiter(";");
                        setFieldExtractor(new BeanWrapperFieldExtractor<>() {
                            {
                                setNames(new String[]{"id", "code", "stockCode", "price", "quantity", "total", "orderDate"});
                            }
                        });
                    }
                })
                .build();
    }

    @Bean
    public ItemReader<Order> databaseReader6() {

        JdbcPagingItemReader<Order> reader = new JdbcPagingItemReader<>();

        reader.setDataSource(this.masterDataSource.masterDataSource());
        reader.setFetchSize(1000);
        reader.setRowMapper(new OrderRowMapper());

        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("*");
        queryProvider.setFromClause("from orders");
        queryProvider.setWhereClause("where id <= " + TOTAL_RECORDS*6/8 + " and id > " + TOTAL_RECORDS*5/8); //====================

        Map<String, org.springframework.batch.item.database.Order> sortKeys = new HashMap<>(1);
        sortKeys.put("id", org.springframework.batch.item.database.Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);
        reader.setQueryProvider(queryProvider);

        return reader;
    }

    @Bean
    public Step asyncManagerStep6(StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory
                .get("Asynchronous Processing : Read -> Process -> Write 6")
                .<Order, Order>chunk(10000)
                .reader(databaseReader6())
                .writer(itemFlatFileWriter6())
                .build();
    }


    @Bean
    public FlatFileItemWriter<Order> itemFlatFileWriter6() {
        return new FlatFileItemWriterBuilder<Order>()
                .name("Writer")
                .append(false)
                .resource(new FileSystemResource("orders6.csv"))
                .lineAggregator(new DelimitedLineAggregator<>() {
                    {
                        setDelimiter(";");
                        setFieldExtractor(new BeanWrapperFieldExtractor<>() {
                            {
                                setNames(new String[]{"id", "code", "stockCode", "price", "quantity", "total", "orderDate"});
                            }
                        });
                    }
                })
                .build();
    }

    @Bean
    public ItemReader<Order> databaseReader7() {

        JdbcPagingItemReader<Order> reader = new JdbcPagingItemReader<>();

        reader.setDataSource(this.masterDataSource.masterDataSource());
        reader.setFetchSize(1000);
        reader.setRowMapper(new OrderRowMapper());

        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("*");
        queryProvider.setFromClause("from orders");
        queryProvider.setWhereClause("where id <= " + TOTAL_RECORDS*7/8 + " and id > " + TOTAL_RECORDS*6/8); //====================

        Map<String, org.springframework.batch.item.database.Order> sortKeys = new HashMap<>(1);
        sortKeys.put("id", org.springframework.batch.item.database.Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);
        reader.setQueryProvider(queryProvider);

        return reader;
    }

    @Bean
    public Step asyncManagerStep7(StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory
                .get("Asynchronous Processing : Read -> Process -> Write 7")
                .<Order, Order>chunk(10000)
                .reader(databaseReader7())
                .writer(itemFlatFileWriter7())
                .build();
    }


    @Bean
    public FlatFileItemWriter<Order> itemFlatFileWriter7() {
        return new FlatFileItemWriterBuilder<Order>()
                .name("Writer")
                .append(false)
                .resource(new FileSystemResource("orders7.csv"))
                .lineAggregator(new DelimitedLineAggregator<>() {
                    {
                        setDelimiter(";");
                        setFieldExtractor(new BeanWrapperFieldExtractor<>() {
                            {
                                setNames(new String[]{"id", "code", "stockCode", "price", "quantity", "total", "orderDate"});
                            }
                        });
                    }
                })
                .build();
    }

    @Bean
    public ItemReader<Order> databaseReader8() {

        JdbcPagingItemReader<Order> reader = new JdbcPagingItemReader<>();

        reader.setDataSource(this.masterDataSource.masterDataSource());
        reader.setFetchSize(1000);
        reader.setRowMapper(new OrderRowMapper());

        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("*");
        queryProvider.setFromClause("from orders");
        queryProvider.setWhereClause("where id <= " + TOTAL_RECORDS + " and id > " + TOTAL_RECORDS*7/8); //====================

        Map<String, org.springframework.batch.item.database.Order> sortKeys = new HashMap<>(1);
        sortKeys.put("id", org.springframework.batch.item.database.Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);
        reader.setQueryProvider(queryProvider);

        return reader;
    }

    @Bean
    public Step asyncManagerStep8(StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory
                .get("Asynchronous Processing : Read -> Process -> Write 8")
                .<Order, Order>chunk(10000)
                .reader(databaseReader8())
                .writer(itemFlatFileWriter8())
                .build();
    }


    @Bean
    public FlatFileItemWriter<Order> itemFlatFileWriter8() {
        return new FlatFileItemWriterBuilder<Order>()
                .name("Writer")
                .append(false)
                .resource(new FileSystemResource("orders8.csv"))
                .lineAggregator(new DelimitedLineAggregator<>() {
                    {
                        setDelimiter(";");
                        setFieldExtractor(new BeanWrapperFieldExtractor<>() {
                            {
                                setNames(new String[]{"id", "code", "stockCode", "price", "quantity", "total", "orderDate"});
                            }
                        });
                    }
                })
                .build();
    }

    @Bean
    public Flow flow1() {
        return new FlowBuilder<SimpleFlow>("flow1")
                .start(asyncManagerStep(null))
                .build();
    }

    @Bean
    public Flow flow2() {
        return new FlowBuilder<SimpleFlow>("flow2")
                .start(asyncManagerStep2(null))
                .build();
    }

    @Bean
    public Flow flow3() {
        return new FlowBuilder<SimpleFlow>("flow3")
                .start(asyncManagerStep3(null))
                .build();
    }

    @Bean
    public Flow flow4() {
        return new FlowBuilder<SimpleFlow>("flow4")
                .start(asyncManagerStep4(null))
                .build();
    }

    @Bean
    public Flow flow5() {
        return new FlowBuilder<SimpleFlow>("flow5")
                .start(asyncManagerStep5(null))
                .build();
    }

    @Bean
    public Flow flow6() {
        return new FlowBuilder<SimpleFlow>("flow5")
                .start(asyncManagerStep6(null))
                .build();
    }

    @Bean
    public Flow flow7() {
        return new FlowBuilder<SimpleFlow>("flow7")
                .start(asyncManagerStep7(null))
                .build();
    }

    @Bean
    public Flow flow8() {
        return new FlowBuilder<SimpleFlow>("flow8")
                .start(asyncManagerStep8(null))
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(16);
        executor.setMaxPoolSize(32);
        executor.setQueueCapacity(128);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        executor.setThreadNamePrefix("MultiThreaded-");
        return executor;
    }

    @Bean
    public Flow parallelFlows() {
        return new FlowBuilder<SimpleFlow>("parallelFlows")
                .split(taskExecutor())
                .add(flow1(), flow2(), flow3(), flow4(), flow5(), flow6(), flow7(), flow8())
                .build();
    }

    @Bean
    public Job processJob() {
        return jobBuilderFactory.get("processJob")
                .incrementer(new RunIdIncrementer())
                .start(parallelFlows())
                .end()
                .build();
    }
}

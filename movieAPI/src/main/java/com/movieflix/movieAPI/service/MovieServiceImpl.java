package com.movieflix.movieAPI.service;

import com.movieflix.movieAPI.dto.MovieDto;
import com.movieflix.movieAPI.entities.Movie;
import com.movieflix.movieAPI.repositories.MovieRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;

    private final FileService fileService;

    @Value("${project.poster}")
    private String path;

    @Value("${base.url}")
    private String baseUrl;

    public MovieServiceImpl(MovieRepository movieRepository, FileService fileService) {
        this.movieRepository = movieRepository;
        this.fileService = fileService;
    }

    @Override
    public MovieDto addMovie(MovieDto movieDto, MultipartFile file) throws IOException {
        // upload the file
        if (Files.exists(Paths.get(path + File.separator + file.getOriginalFilename()))){
            throw new RuntimeException("File already exists! Please enter another file name.");
        }
        String uploadedFileName = fileService.uploadFile(path, file);

        // set the value of field 'poster' as a filename
        movieDto.setPoster(uploadedFileName);

        // map dto to Movie object
        Movie movie = new Movie(
                null,
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );

        // save the movie object
        Movie saveMovie = movieRepository.save(movie);

        // generate the poster url
        String posterUrl = baseUrl + "/file/" + uploadedFileName;

        // map movie object ot DTP object and return it
        MovieDto response = new MovieDto(
                saveMovie.getMovieId(),
                saveMovie.getTitle(),
                saveMovie.getDirector(),
                saveMovie.getStudio(),
                saveMovie.getMovieCast(),
                saveMovie.getReleaseYear(),
                saveMovie.getPoster(),
                posterUrl
        );

        return response;
    }

    @Override
    public MovieDto getMovie(Integer movieId) {
        // Check the data in the DB and if exits, fetch the data of the given ID
        Movie movie = movieRepository.findById(movieId).orElseThrow(()-> new RuntimeException("Movie not found!"));

        // generate posterUrl
        String posterUrl = baseUrl + "/file/" + movie.getPoster();

        // map to movieDto object and return it
        MovieDto MovieDtos = new MovieDto(
                movie.getMovieId(),
                movie.getTitle(),
                movie.getDirector(),
                movie.getStudio(),
                movie.getMovieCast(),
                movie.getReleaseYear(),
                movie.getPoster(),
                posterUrl
        );
        return MovieDtos;
    }

    @Override
    public List<MovieDto> getAllMovies() {
        // fetch all the data from DB
        List<Movie> movies = movieRepository.findAll();

        List<MovieDto> movieDtos = new ArrayList<>();

        // Iterate through the list, generate posterUrl for each movie object
        for (Movie movie : movies){
            String posterUrl = baseUrl + "/file/" + movie.getPoster();
            MovieDto movieDto = new MovieDto(
                    movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl
            );
            movieDtos.add(movieDto);

        }
        // and map to movieDto obj
        return movieDtos;
    }

    @Override
    public MovieDto updateMovie(Integer movieId, MovieDto movieDto, MultipartFile file) throws IOException {
        // 1.check if movie object exists with given movieId
        Movie mv = movieRepository.findById(movieId).orElseThrow(()-> new RuntimeException("Movie not found!"));

        //2.if file is null, do nothing
        // if file is not null, then delete existing file associate with the record,
        // and upload the new file
        String fileName = mv.getPoster();
        if (file != null){
            Files.deleteIfExists(Paths.get(path + File.separator + fileName));
            fileName = fileService.uploadFile(path, file);
        }

        //3. set movieDtos poster value, according to setup2
        movieDto.setPoster(fileName);

        //4. map it to movie object
        Movie movie = new Movie(
                mv.getMovieId(),
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );


        //5. save the movie object -> return saved movie object
        Movie updatedMovie = movieRepository.save(movie);

        //6. generate postureUrl for it
        String posterUrl = baseUrl + File.separator + fileName;

        //7. map to movieDto and return it
        MovieDto response = new MovieDto(
                movie.getMovieId(),
                movie.getTitle(),
                movie.getDirector(),
                movie.getStudio(),
                movie.getMovieCast(),
                movie.getReleaseYear(),
                movie.getPoster(),
                posterUrl
        );

        return response;
    }

    @Override
    public String deleteMovie(Integer movieId) throws IOException {
        //1. check if movie object exits in DB
        Movie mv = movieRepository.findById(movieId).orElseThrow(()-> new RuntimeException("Movie not found!"));
        Integer id = mv.getMovieId();

        //2. delete the file
        Files.deleteIfExists(Paths.get(path + File.separator + mv.getPoster()));

        //3. delete the movie
        movieRepository.delete(mv);


        return "Movie deleted with id = " + id;
    }
}

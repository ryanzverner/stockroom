Sequel.migration do
  up do
    create_table(:sows) do
      primary_key :id
      Integer :hourly_rate, :size => 5
      DateTime :start
      DateTime :end
      String :url
      DateTime :signed_date

      DateTime :created_at
      DateTime :updated_at
    end
  end

  down do
    drop_table(:sows)
  end
end
